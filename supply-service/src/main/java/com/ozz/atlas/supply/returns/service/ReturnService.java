package com.ozz.atlas.supply.returns.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
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
import com.ozz.atlas.supply.returns.dtos.ReturnStatusHistoryResponseDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnStatusDto;
import com.ozz.atlas.supply.returns.exception.ReturnErrorCode;
import com.ozz.atlas.supply.returns.exception.ReturnException;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.returns.repository.ReturnStatusHistoryRepository;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

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

                    Long maxQtyValue = poItem.getConfirmedQty() != null
                            ? poItem.getConfirmedQty()
                            : poItem.getOrderedQty();

                    BigDecimal maxQty = BigDecimal.valueOf(maxQtyValue);

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
        appendReturnEvent(
                EventTypes.RETURN_REQUEST_CREATED,
                savedReturn,
                resolveSourceShipment(savedReturn),
                actorPublicId,
                savedReturn.getRequestOrganizationPublicId(),
                "반품 요청 생성",
                "반품 요청 생성 시"
        );

        return toResponseDto(savedReturn);
    }

    public Page<ReturnRequestResponseDto> getAllReturns(Pageable pageable) {
        return returnRequestRepository.findAll(pageable)
                .map(this::toResponseDto);
    }

    public ReturnRequestResponseDto getReturnByPublicId(String publicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));
        return toResponseDto(returnRequest);
    }

    @Transactional
    public ReturnRequestResponseDto updateReturn(String publicId, UpdateReturnRequestDto request, String actorPublicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        String attachments = request.getAttachmentPublicIds() != null ? String.join(",", request.getAttachmentPublicIds()) : null;
        returnRequest.update(request.getReturnType(), request.getReturnReason(), attachments);

        // 수정된 반품 정보를 ES에도 다시 저장
        returnSearchService.saveReturnDocument(returnRequest);

        return toResponseDto(returnRequest);
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
        appendReturnEvent(
                resolveReturnStatusEventType(returnRequest.getReturnStatus()),
                returnRequest,
                resolveSourceShipment(returnRequest),
                actorPublicId,
                returnRequest.getRequestOrganizationPublicId(),
                "반품 상태 변경",
                request.getReason() != null && !request.getReason().isBlank() ? request.getReason() : "반품 상태 변경 시"
        );

        return toResponseDto(returnRequest);
    }

    public List<ReturnStatusHistoryResponseDto> getReturnHistories(String publicId) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));
        return returnStatusHistoryRepository.findByReturnRequestIdOrderByRecordedAtDesc(returnRequest.getId())
                .stream()
                .map(ReturnStatusHistoryResponseDto::from)
                .collect(Collectors.toList());
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

    private ReturnRequestResponseDto toResponseDto(ReturnRequest returnRequest) {
        String reqOrgName = null;
        if (returnRequest.getRequestOrganizationPublicId() != null) {
            SupplySupplier reqSupplier = supplierRepository.findByOrganizationPublicId(returnRequest.getRequestOrganizationPublicId()).orElse(null);
            if (reqSupplier != null) {
                reqOrgName = reqSupplier.getSupplierName();
            }
        }

        String tgtOrgName = null;
        if (returnRequest.getTargetOrganizationPublicId() != null) {
            SupplySupplier tgtSupplier = supplierRepository.findByOrganizationPublicId(returnRequest.getTargetOrganizationPublicId()).orElse(null);
            if (tgtSupplier != null) {
                tgtOrgName = tgtSupplier.getSupplierName();
            }
        }

        Map<String, String> itemNames = new HashMap<>();
        if (returnRequest.getItems() != null) {
            for (ReturnItem item : returnRequest.getItems()) {
                if (item.getItemPublicId() != null && !itemNames.containsKey(item.getItemPublicId())) {
                    SupplyItem supplyItem = supplyItemRepository.findByPublicId(item.getItemPublicId()).orElse(null);
                    if (supplyItem != null) {
                        itemNames.put(item.getItemPublicId(), supplyItem.getItemName());
                    }
                }
            }
        }

        return ReturnRequestResponseDto.from(returnRequest, reqOrgName, tgtOrgName, itemNames);
    }

    private void appendReturnEvent(
            String eventType,
            ReturnRequest returnRequest,
            Shipment shipment,
            String actorUserPublicId,
            String organizationPublicId,
            String eventName,
            String description
    ) {
        SupplyChainContext context = supplyChainContextResolver.fromReturn(returnRequest, shipment);
        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_RETURN_REQUEST,
                        eventType,
                        AggregateType.RETURN_REQUEST,
                        returnRequest.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        context,
                        supplyDomainEventFactory.payload(
                                returnRequest.getPublicId(),
                                returnRequest.getReturnNumber(),
                                returnRequest.getReturnStatus().name(),
                                eventName,
                                description,
                                returnRequest.getReturnType().name()
                        )
                )
        );
    }

    private Shipment resolveSourceShipment(ReturnRequest returnRequest) {
        if (returnRequest.getSourceShipmentPublicId() == null || returnRequest.getSourceShipmentPublicId().isBlank()) {
            return null;
        }
        return shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId()).orElse(null);
    }

    private String resolveReturnStatusEventType(ReturnStatus returnStatus) {
        if (returnStatus == ReturnStatus.APPROVED) {
            return EventTypes.RETURN_REQUEST_APPROVED;
        }
        if (returnStatus == ReturnStatus.REJECTED) {
            return EventTypes.RETURN_REQUEST_REJECTED;
        }
        if (returnStatus == ReturnStatus.COMPLETED) {
            return EventTypes.RETURN_REQUEST_COMPLETED;
        }
        return EventTypes.RETURN_REQUEST_CREATED;
    }
}
