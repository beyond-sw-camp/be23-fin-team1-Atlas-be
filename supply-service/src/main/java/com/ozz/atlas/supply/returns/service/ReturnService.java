package com.ozz.atlas.supply.returns.service;

import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
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
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

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
                .returnNumber(generateReturnNumber(shipment))
                .sourceShipmentPublicId(request.getSourceShipmentPublicId())
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
            returnRequest.update(null, null, null, newAttachments);
        }

        if (beforeStatus != ReturnStatus.APPROVED && request.getReturnStatus() == ReturnStatus.APPROVED) {
            if (returnRequest.getResolutionType() != com.ozz.atlas.supply.returns.domain.ResolutionType.DISPOSAL) {
                createReturnShipment(returnRequest);
            }
        }

        if (beforeStatus == ReturnStatus.RECEIVED && request.getReturnStatus() == ReturnStatus.RESHIPPED) {
            createExchangeShipment(returnRequest);
        }

        if (request.getReturnStatus() == ReturnStatus.COMPLETED) {
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
            if (!organizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
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

    private String generateReturnNumber(Shipment shipment) {
        String baseNumber = shipment.getShipmentNumber();
        long sequence = returnRequestRepository.count() + 1;

        return "RTN-" + baseNumber + "-" + String.format("%03d", sequence);
    }
    private void createReturnShipment(ReturnRequest returnRequest) {
        if (returnRequest.getReturnShipmentPublicId() != null
                && !returnRequest.getReturnShipmentPublicId().isBlank()) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

        Shipment sourceShipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        String returnShipmentNumber = generateReturnShipmentNumber(returnRequest);

        if (shipmentRepository.existsByShipmentNumber(returnShipmentNumber)) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

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

        returnRequest.assignReturnShipmentPublicId(savedReturnShipment.getPublicId());
    }

    private void createExchangeShipment(ReturnRequest returnRequest) {
        Shipment sourceShipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new ReturnException(ReturnErrorCode.RETURN_NOT_FOUND));

        String exchangeShipmentNumber = "EX-" + returnRequest.getReturnNumber();

        if (shipmentRepository.existsByShipmentNumber(exchangeShipmentNumber)) {
            throw new ReturnException(ReturnErrorCode.INVALID_RETURN_REQUEST);
        }

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
        
        // Note: For EXCHANGE, we might want to track this new shipment somewhere, but ReturnRequest currently only has `returnShipmentPublicId` which is used for the backward flow.
    }

    private String generateReturnShipmentNumber(ReturnRequest returnRequest) {
        return "RS-" + returnRequest.getReturnNumber();
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
