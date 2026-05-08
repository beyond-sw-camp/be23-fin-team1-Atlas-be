package com.ozz.atlas.supply.returns.service;

import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.common.code.SequenceCodeType;
import com.ozz.atlas.supply.common.code.YearlySequenceCodeGenerator;
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
import com.ozz.atlas.supply.returns.dtos.*;
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
import com.ozz.atlas.supply.inventory.service.ItemInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import com.ozz.atlas.supply.settlement.service.SettlementService;

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
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ShipmentSearchService shipmentSearchService;
    private final SettlementService settlementService;

    public boolean existsByPublicId(String publicId) {
        return returnRequestRepository.existsByPublicId(publicId);
    }
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;
    private final ItemInventoryService itemInventoryService;

    @Transactional
    public ReturnRequestResponseDto createReturn(
            CreateReturnRequestDto request,
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnCreateActor(actorPublicId, organizationPublicId, organizationType, userRole);

        Shipment shipment = shipmentRepository.findByPublicId(request.getSourceShipmentPublicId())
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        validateReturnSourceShipment(shipment);

        LogisticsNode originNode = getShipmentNode(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getShipmentNode(shipment.getDestinationNodeId());

        String requestOrganizationPublicId = destinationNode.getOrganizationPublicId();
        String targetOrganizationPublicId = originNode.getOrganizationPublicId();

        if (!requestOrganizationPublicId.equals(organizationPublicId)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }

        String masterAttachments = request.getAttachmentPublicIds() != null
                ? String.join(",", request.getAttachmentPublicIds())
                : null;

        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnNumber(generateNextReturnNumber())
                .sourceShipmentPublicId(request.getSourceShipmentPublicId())
                .sourceShipmentId(shipment.getId())
                .requestOrganizationPublicId(requestOrganizationPublicId)
                .targetOrganizationPublicId(targetOrganizationPublicId)
                .returnType(request.getReturnType())
                .resolutionType(request.getResolutionType())
                .returnReason(request.getReturnReason())
                .createdByUserPublicId(actorPublicId)
                .attachmentPublicIds(masterAttachments)
                .build();

        request.getItems().forEach(itemDto -> {
            String itemAttachments = itemDto.getAttachmentPublicIds() != null ? String.join(",", itemDto.getAttachmentPublicIds()) : null;
            ReturnItem returnItem = ReturnItem.builder()
                    .itemPublicId(itemDto.getItemPublicId())
                    .returnQty(itemDto.getReturnQty())
                    .unit(itemDto.getUnit())
                    .detailReason(itemDto.getDetailReason())
                    .attachmentPublicIds(itemAttachments)
                    .build();
            returnRequest.addItem(returnItem);
        });

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

    public Page<ReturnRequestResponseDto> getAllReturns(
            Pageable pageable,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnReadableActor(organizationPublicId, organizationType, userRole);

        return returnRequestRepository
                .findByRequestOrganizationPublicIdOrTargetOrganizationPublicId(
                        organizationPublicId,
                        organizationPublicId,
                        pageable
                )
                .map(this::toResponseDto);
    }

    public ReturnRequestResponseDto getReturnByPublicId(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnReadableActor(organizationPublicId, organizationType, userRole);

        ReturnRequest returnRequest = returnRequestRepository
                .findByPublicIdAndRequestOrganizationPublicIdOrPublicIdAndTargetOrganizationPublicId(
                        publicId,
                        organizationPublicId,
                        publicId,
                        organizationPublicId
                )
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        return toResponseDto(returnRequest);
    }

    public boolean existsReturnByPublicId(String publicId) {
        return returnRequestRepository.findByPublicId(publicId).isPresent();
    }

    @Transactional
    public ReturnRequestResponseDto updateReturn(
            String publicId,
            UpdateReturnRequestDto request,
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnWritableActor(actorPublicId, organizationPublicId, organizationType, userRole);

        ReturnRequest returnRequest = returnRequestRepository
                .findByPublicIdAndRequestOrganizationPublicIdOrPublicIdAndTargetOrganizationPublicId(
                        publicId,
                        organizationPublicId,
                        publicId,
                        organizationPublicId
                )
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        if (!organizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }
        validateReturnUpdatable(returnRequest);

        String attachments = request.getAttachmentPublicIds() != null ? String.join(",", request.getAttachmentPublicIds()) : null;
        returnRequest.update(request.getReturnType(), request.getResolutionType(), request.getReturnReason(), attachments);

        // 수정된 반품 정보를 ES에도 다시 저장
        returnSearchService.saveReturnDocument(returnRequest);

        return toResponseDto(returnRequest);
    }

    @Transactional
    public ReturnRequestResponseDto changeStatus(
            String publicId,
            UpdateReturnStatusDto request,
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnStatusActor(actorPublicId, organizationPublicId, organizationType, userRole);

        ReturnRequest returnRequest = returnRequestRepository
                .findByPublicIdAndRequestOrganizationPublicIdOrPublicIdAndTargetOrganizationPublicId(
                        publicId,
                        organizationPublicId,
                        publicId,
                        organizationPublicId
                )
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        validateReturnStatusTransition(returnRequest, request.getReturnStatus(), organizationPublicId);

        ReturnStatus beforeStatus = returnRequest.getReturnStatus();
        returnRequest.changeStatus(request.getReturnStatus());

        // 증빙 파일 업데이트 (폐기 등)
        if (request.getAttachmentPublicIds() != null && !request.getAttachmentPublicIds().isEmpty()) {
            String newAttachments = String.join(",", request.getAttachmentPublicIds());
            returnRequest.updateAttachments(newAttachments);
            
            // 만약 폐기 상태로 가는데 증빙이 있다면 첫번째 파일을 증빙ID로 설정 (간이 구현)
            if (request.getReturnStatus() == ReturnStatus.DISPOSED || request.getReturnStatus() == ReturnStatus.COMPLETED) {
                if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                     returnRequest.getItems().forEach(item -> {
                         item.setDisposalInfo(request.getReason(), request.getAttachmentPublicIds().get(0));
                     });
                }
            }
        }

        if (beforeStatus != ReturnStatus.APPROVED && request.getReturnStatus() == ReturnStatus.APPROVED) {
            // 반품 승인은 '대상 조직(공급사)'이 수행함.
            // 교환 시 재고 선점
            if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
                reserveExchangeStock(returnRequest);
                createReturnShipment(returnRequest, actorPublicId);
            } else if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.RETURN) {
                createReturnShipment(returnRequest, actorPublicId);
            }
        }

        if (beforeStatus != ReturnStatus.RESHIPPED && request.getReturnStatus() == ReturnStatus.RESHIPPED) {
            if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
                createExchangeShipment(returnRequest, actorPublicId);
            }
        }

        if (request.getReturnStatus() == ReturnStatus.COMPLETED) {
            // 재고 반영 (반품 입고 / 불량 처리 등)
            processInventoryOnCompletion(returnRequest);
            settlementService.createReturnSettlementIfAbsent(returnRequest.getPublicId());
        }

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

    @Transactional
    public ReturnRequestResponseDto inspectItem(
            String publicId,
            Long itemId,
            InspectReturnItemRequestDto request,
            String actorPublicId,
            String organizationPublicId
    ) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
             throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }

        if (returnRequest.getReturnStatus() != ReturnStatus.INSPECTING && returnRequest.getReturnStatus() != ReturnStatus.RECEIVED) {
            throw new IllegalStateException("검수 가능한 상태가 아닙니다.");
        }

        ReturnItem item = returnRequest.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("품목을 찾을 수 없습니다."));

        item.inspect(request.getQcStatus(), request.getQcGrade(), request.getDescription());
        
        returnSearchService.saveReturnDocument(returnRequest);
        return toResponseDto(returnRequest);
    }

    private void reserveExchangeStock(ReturnRequest returnRequest) {
        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(returnRequest.getTargetOrganizationPublicId())
                .orElseThrow(() -> new IllegalStateException("공급사를 찾을 수 없습니다."));

        for (ReturnItem item : returnRequest.getItems()) {
            SupplyItem supplyItem = supplyItemRepository.findByPublicId(item.getItemPublicId())
                    .orElseThrow(() -> new IllegalStateException("품목을 찾을 수 없습니다."));
            
            itemInventoryService.reserveForExchange(supplier, supplyItem, item.getReturnQty().longValue(), returnRequest.getPublicId());
        }
    }

    private void processInventoryOnCompletion(ReturnRequest returnRequest) {
        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(returnRequest.getTargetOrganizationPublicId())
                .orElseThrow(() -> new IllegalStateException("공급사를 찾을 수 없습니다."));

        for (ReturnItem item : returnRequest.getItems()) {
            SupplyItem supplyItem = supplyItemRepository.findByPublicId(item.getItemPublicId())
                    .orElseThrow(() -> new IllegalStateException("품목을 찾을 수 없습니다."));

            if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                // 폐기 처리
                itemInventoryService.deductForDisposal(supplier, supplyItem, item.getReturnQty().longValue(), returnRequest.getPublicId());
            } else {
                // 반납/교환 후 입고 처리 (검수 결과 반영)
                itemInventoryService.processReturnInventory(supplier, supplyItem, item.getReturnQty().longValue(), item.getQcGrade(), returnRequest.getPublicId());
            }
        }
    }

    public List<ReturnStatusHistoryResponseDto> getReturnHistories(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnReadableActor(organizationPublicId, organizationType, userRole);

        ReturnRequest returnRequest = returnRequestRepository
                .findByPublicIdAndRequestOrganizationPublicIdOrPublicIdAndTargetOrganizationPublicId(
                        publicId,
                        organizationPublicId,
                        publicId,
                        organizationPublicId
                )
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

        String settlementPublicId = settlementService.getSettlementPublicIdByTargetPublicId(
                returnRequest.getPublicId(), 
                com.ozz.atlas.supply.settlement.domain.SettlementTargetType.RETURN
        );

        return ReturnRequestResponseDto.from(returnRequest, reqOrgName, tgtOrgName, itemNames, settlementPublicId);
    }
    private LogisticsNode getShipmentNode(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));
    }
    private void validateReturnCreateActor(
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (actorPublicId == null || actorPublicId.isBlank()
                || organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        if ("ADMIN".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(userRole)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }
    }
    private void validateReturnReadableActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        if ("ADMIN".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(userRole)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }

        if (!"BUYER".equalsIgnoreCase(organizationType)
                && !"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }
    }
    private void validateReturnWritableActor(
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (actorPublicId == null || actorPublicId.isBlank()
                || organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        if ("ADMIN".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(userRole)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }

        if (!"BUYER".equalsIgnoreCase(organizationType)
                && !"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }
    }

    private void validateReturnStatusActor(
            String actorPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (actorPublicId == null || actorPublicId.isBlank()
                || organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        if ("ADMIN".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(userRole)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }

        if (!"BUYER".equalsIgnoreCase(organizationType)
                && !"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
        }
    }

    private void validateReturnStatusTransition(
            ReturnRequest returnRequest,
            ReturnStatus nextStatus,
            String organizationPublicId
    ) {
        ReturnStatus currentStatus = returnRequest.getReturnStatus();
        com.ozz.atlas.supply.returns.domain.ResolutionType resolutionType = returnRequest.getResolutionType();

        if (currentStatus == ReturnStatus.REQUESTED) {
            if (nextStatus != ReturnStatus.APPROVED && nextStatus != ReturnStatus.REJECTED) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }

            if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
            }

            return;
        }

        if (currentStatus == ReturnStatus.APPROVED) {
            if (resolutionType == com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                if (nextStatus != ReturnStatus.DISPOSED) {
                    throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
                }
                if (!organizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
                    throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
                }
                return;
            } else {
                if (nextStatus != ReturnStatus.IN_TRANSIT) {
                    throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
                }
                if (!organizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
                    throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
                }
                return;
            }
        }

        if (currentStatus == ReturnStatus.IN_TRANSIT) {
            if (resolutionType == com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (nextStatus != ReturnStatus.RECEIVED) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
            }
            return;
        }

        if (currentStatus == ReturnStatus.RECEIVED) {
            if (resolutionType == com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (nextStatus != ReturnStatus.INSPECTING) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
            }
            return;
        }

        if (currentStatus == ReturnStatus.INSPECTING) {
            if (resolutionType == com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
                if (nextStatus != ReturnStatus.RESHIPPED) {
                    throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
                }
                if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                    throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
                }
                return;
            } else {
                if (nextStatus != ReturnStatus.COMPLETED) {
                    throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
                }
                if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                    throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
                }
                return;
            }
        }

        if (currentStatus == ReturnStatus.RESHIPPED) {
            if (resolutionType != com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (nextStatus != ReturnStatus.COMPLETED) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (!organizationPublicId.equals(returnRequest.getRequestOrganizationPublicId()) &&
                !organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
            }
            return;
        }

        if (currentStatus == ReturnStatus.DISPOSED) {
            if (resolutionType != com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (nextStatus != ReturnStatus.COMPLETED) {
                throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
            }
            if (!organizationPublicId.equals(returnRequest.getTargetOrganizationPublicId())) {
                throw new ReturnException(ReturnErrorCode.FORBIDDEN_RETURN_CREATE);
            }

            return;
        }

        throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
    }
    private void validateReturnUpdatable(ReturnRequest returnRequest) {
        if (returnRequest.getReturnStatus() != ReturnStatus.REQUESTED) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_STATUS_TRANSITION);
        }
    }

    private void validateReturnSourceShipment(Shipment shipment) {
        if (shipment.getStatus() != ShipmentStatus.ARRIVED) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_SOURCE_SHIPMENT);
        }

        if (returnRequestRepository.existsBySourceShipmentPublicId(shipment.getPublicId())) {
            throw new ReturnException(ReturnErrorCode.DUPLICATE_RETURN_REQUEST);
        }
    }

    private String generateNextReturnNumber() {
        String prefix = YearlySequenceCodeGenerator.currentPrefix(SequenceCodeType.RETURN);
        String lastReturnNumber = returnRequestRepository
                .findTopByReturnNumberStartingWithOrderByReturnNumberDesc(prefix)
                .map(ReturnRequest::getReturnNumber)
                .orElse(null);

        String candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.RETURN, lastReturnNumber, 7);
        while (returnRequestRepository.existsByReturnNumber(candidate)) {
            candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.RETURN, candidate, 7);
        }

        return candidate;
    }
    private void createReturnShipment(ReturnRequest returnRequest, String actorPublicId) {
        if (returnRequest.getReturnShipmentPublicId() != null
                && !returnRequest.getReturnShipmentPublicId().isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        Shipment sourceShipment = resolveRequiredSourceShipment(returnRequest);

        String returnShipmentNumber = generateNextShipmentNumber(SequenceCodeType.RETURN_SHIPMENT);

        Shipment returnShipment = Shipment.builder()
                .shipmentNumber(returnShipmentNumber)
                .poId(sourceShipment.getPoId())
                .subPoId(sourceShipment.getSubPoId())
                .purchaseOrderPublicId(sourceShipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(sourceShipment.getSubPurchaseOrderPublicId())
                .sourceType(ShipmentSourceType.RETURN)
                .sourcePublicId(returnRequest.getPublicId())
                .carrierName(null)
                .vehicleNo(null)
                .trackingNo(null)
                .originNodeId(sourceShipment.getDestinationNodeId())
                .destinationNodeId(sourceShipment.getOriginNodeId())
                .currentNodeId(sourceShipment.getDestinationNodeId())
                .departureEta(null)
                .arrivalEta(null)
                .status(ShipmentStatus.READY)
                .temperatureRequired(sourceShipment.isTemperatureRequired())
                .sealedPackagingRequired(sourceShipment.isSealedPackagingRequired())
                .fragile(sourceShipment.isFragile())
                .build();

        Shipment savedReturnShipment = shipmentRepository.save(returnShipment);
        shipmentSearchService.saveShipmentDocument(savedReturnShipment);

        returnRequest.assignReturnShipment(savedReturnShipment.getId(), savedReturnShipment.getPublicId());
    }

    private void createExchangeShipment(ReturnRequest returnRequest, String actorPublicId) {
        Shipment sourceShipment = resolveRequiredSourceShipment(returnRequest);

        String exchangeShipmentNumber = generateNextShipmentNumber(SequenceCodeType.EXCHANGE_SHIPMENT);

        Shipment exchangeShipment = Shipment.builder()
                .shipmentNumber(exchangeShipmentNumber)
                .poId(sourceShipment.getPoId())
                .subPoId(sourceShipment.getSubPoId())
                .purchaseOrderPublicId(sourceShipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(sourceShipment.getSubPurchaseOrderPublicId())
                .sourceType(ShipmentSourceType.EXCHANGE)
                .sourcePublicId(returnRequest.getPublicId())
                .carrierName(null)
                .vehicleNo(null)
                .trackingNo(null)
                .originNodeId(sourceShipment.getOriginNodeId())
                .destinationNodeId(sourceShipment.getDestinationNodeId())
                .currentNodeId(sourceShipment.getOriginNodeId())
                .departureEta(null)
                .arrivalEta(null)
                .status(ShipmentStatus.READY)
                .temperatureRequired(sourceShipment.isTemperatureRequired())
                .sealedPackagingRequired(sourceShipment.isSealedPackagingRequired())
                .fragile(sourceShipment.isFragile())
                .build();

        Shipment savedExchangeShipment = shipmentRepository.save(exchangeShipment);
        shipmentSearchService.saveShipmentDocument(savedExchangeShipment);

        returnRequest.assignExchangeShipment(savedExchangeShipment.getId(), savedExchangeShipment.getPublicId());
    }

    private String generateNextShipmentNumber(SequenceCodeType type) {
        String prefix = YearlySequenceCodeGenerator.currentPrefix(type);
        String lastShipmentNumber = shipmentRepository
                .findTopByShipmentNumberStartingWithOrderByShipmentNumberDesc(prefix)
                .map(Shipment::getShipmentNumber)
                .orElse(null);

        String candidate = YearlySequenceCodeGenerator.next(type, lastShipmentNumber, 7);
        while (shipmentRepository.existsByShipmentNumber(candidate)) {
            candidate = YearlySequenceCodeGenerator.next(type, candidate, 7);
        }

        return candidate;
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
        if (returnRequest.getSourceShipmentId() != null) {
            return shipmentRepository.findById(returnRequest.getSourceShipmentId()).orElse(null);
        }
        if (returnRequest.getSourceShipmentPublicId() == null || returnRequest.getSourceShipmentPublicId().isBlank()) {
            return null;
        }
        return shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId()).orElse(null);
    }

    private Shipment resolveRequiredSourceShipment(ReturnRequest returnRequest) {
        Shipment sourceShipment = resolveSourceShipment(returnRequest);
        if (sourceShipment == null) {
            throw new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND);
        }
        return sourceShipment;
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
