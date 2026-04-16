package com.ozz.atlas.supply.returns.service;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnItem;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnStatusHistory;
import com.ozz.atlas.supply.returns.dtos.CreateReturnItemDto;
import com.ozz.atlas.supply.returns.dtos.CreateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnStatusDto;
import com.ozz.atlas.supply.returns.exception.ReturnErrorCode;
import com.ozz.atlas.supply.returns.exception.ReturnException;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.returns.repository.ReturnStatusHistoryRepository;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnStatusHistoryRepository returnStatusHistoryRepository;
    private final ShipmentRepository shipmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ReturnSearchService returnSearchService;

    @Transactional
    public ReturnRequestResponseDto createReturn(CreateReturnRequestDto request, String actorPublicId) {
        String masterAttachments = request.getAttachmentPublicIds() != null ? String.join(",", request.getAttachmentPublicIds()) : null;

        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnNumber(request.getReturnNumber())
                .sourceShipmentPublicId(request.getSourceShipmentPublicId())
                .requestOrganizationPublicId(request.getRequestOrganizationPublicId())
                .targetOrganizationPublicId(request.getTargetOrganizationPublicId())
                .returnType(request.getReturnType())
                .returnReason(request.getReturnReason())
                .createdByUserPublicId(actorPublicId)
                .attachmentPublicIds(masterAttachments)
                .build();

        request.getItems().forEach(itemDto -> {
            String itemAttachments = itemDto.getAttachmentPublicIds() != null ? String.join(",", itemDto.getAttachmentPublicIds()) : null;
            ReturnItem returnItem = ReturnItem.builder()
                    .itemPublicId(itemDto.getItemPublicId())
                    .lotPublicId(itemDto.getLotPublicId())
                    .returnQty(itemDto.getReturnQty())
                    .unit(itemDto.getUnit())
                    .detailReason(itemDto.getDetailReason())
                    .attachmentPublicIds(itemAttachments)
                    .build();
            returnRequest.addItem(returnItem);
        });

        if (request.getSourceShipmentPublicId() != null && !request.getSourceShipmentPublicId().isBlank()) {
            Shipment shipment = shipmentRepository.findByPublicId(request.getSourceShipmentPublicId())
                    .orElseThrow(() -> new IllegalStateException("출하 정보를 찾을 수 없습니다."));

            if (shipment.getPoId() != null) {
                SupplyPurchaseOrder po = purchaseOrderRepository.findById(shipment.getPoId())
                        .orElseThrow(() -> new IllegalStateException("발주 정보를 찾을 수 없습니다."));

                for (CreateReturnItemDto itemDto : request.getItems()) {
                    SupplyPurchaseOrderItem poItem = po.getPurchaseOrderItems().stream()
                            .filter(pi -> pi.getItem().getPublicId().equals(itemDto.getItemPublicId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("발주에 해당 품목이 존재하지 않습니다: " + itemDto.getItemPublicId()));

                    BigDecimal maxQty = poItem.getConfirmedQty() != null ? poItem.getConfirmedQty() : poItem.getOrderedQty();
                    if (itemDto.getReturnQty().compareTo(maxQty) > 0) {
                        throw new ReturnException(ReturnErrorCode.INVALID_RETURN_QUANTITY);
                    }
                }
            }
        }

        ReturnRequest savedReturn = returnRequestRepository.save(returnRequest);

        saveHistory(savedReturn.getId(), null, savedReturn.getReturnStatus(), "반품 요청 생성", actorPublicId);

        // DB 저장이 끝난 반품 데이터를 ES에도 같이 저장
        returnSearchService.saveReturnDocument(savedReturn);

        return ReturnRequestResponseDto.from(savedReturn);
    }

    public Page<ReturnRequestResponseDto> getAllReturns(Pageable pageable) {
        return returnRequestRepository.findAll(pageable)
                .map(ReturnRequestResponseDto :: from);
    }

    public ReturnRequestResponseDto getReturnByPublicId(String publicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));
        return ReturnRequestResponseDto.from(returnRequest);
    }

    @Transactional
    public ReturnRequestResponseDto updateReturn(String publicId, UpdateReturnRequestDto request, String actorPublicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        String attachments = request.getAttachmentPublicIds() != null ? String.join(",", request.getAttachmentPublicIds()) : null;
        returnRequest.update(request.getReturnType(), request.getReturnReason(), attachments);

        // 수정된 반품 정보를 ES에도 다시 저장
        returnSearchService.saveReturnDocument(returnRequest);

        return ReturnRequestResponseDto.from(returnRequest);
    }

    @Transactional
    public ReturnRequestResponseDto changeStatus(String publicId, UpdateReturnStatusDto request, String actorPublicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        ReturnStatus beforeStatus = returnRequest.getReturnStatus();
        returnRequest.changeStatus(request.getReturnStatus());

        saveHistory(returnRequest.getId(), beforeStatus, returnRequest.getReturnStatus(), request.getReason(), actorPublicId);

        // 상태가 바뀌었으니 ES 문서도 다시 저장
        returnSearchService.saveReturnDocument(returnRequest);

        return ReturnRequestResponseDto.from(returnRequest);
    }

    private void saveHistory(Long returnRequestId, ReturnStatus before, ReturnStatus after, String reason, String actor) {
        ReturnStatusHistory history = ReturnStatusHistory.builder()
                .returnRequestId(returnRequestId)
                .beforeStatus(before)
                .afterStatus(after)
                .reason(reason)
                .recordedBy(actor)
                .build();
        returnStatusHistoryRepository.save(history);
    }
}