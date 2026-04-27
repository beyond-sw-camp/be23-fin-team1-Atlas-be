package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.shipment.ShipmentFactory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.shipment.domain.CheckpointStatus;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.EtaBasis;
import com.ozz.atlas.supply.shipment.domain.EtaProjection;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentCheckpoint;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.EtaProjectionResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentEtaResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentStatusHistoryResponseDto;
import com.ozz.atlas.supply.shipment.dtos.TrackShipmentRequestDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.EtaProjectionRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentStatusHistoryRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.shipment.dtos.UpdateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentMapCheckpointDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentMapResponseDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ShipmentService {

    private static final String ADMIN_ORGANIZATION_TYPE = "ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";

    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository shipmentCheckpointRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final LogisticsNodeService logisticsNodeService;
    private final ShipmentSearchService shipmentSearchService;
    private final EtaProjectionRepository etaProjectionRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final ShipmentFactory shipmentFactory;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final SupplyChainContextResolver supplyChainContextResolver;
    private final SupplyDomainEventFactory supplyDomainEventFactory;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentCheckpointRepository shipmentCheckpointRepository,
            ShipmentStatusHistoryRepository shipmentStatusHistoryRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            LogisticsNodeService logisticsNodeService,
            ShipmentSearchService shipmentSearchService,
            EtaProjectionRepository etaProjectionRepository,
            OutboxEventAppender outboxEventAppender,
            ShipmentFactory shipmentFactory,
            PurchaseOrderRepository purchaseOrderRepository,
            SubPurchaseOrderRepository subPurchaseOrderRepository,
            ReturnRequestRepository returnRequestRepository,
            SupplyChainContextResolver supplyChainContextResolver,
            SupplyDomainEventFactory supplyDomainEventFactory
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentCheckpointRepository = shipmentCheckpointRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.logisticsNodeService = logisticsNodeService;
        this.shipmentSearchService = shipmentSearchService;
        this.etaProjectionRepository = etaProjectionRepository;
        this.outboxEventAppender = outboxEventAppender;
        this.shipmentFactory = shipmentFactory;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.subPurchaseOrderRepository = subPurchaseOrderRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.supplyChainContextResolver = supplyChainContextResolver;
        this.supplyDomainEventFactory = supplyDomainEventFactory;
    }

    // 출하 생성
    public ShipmentResponseDto createShipment(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateCreateShipmentActor(organizationPublicId, organizationType, userRole);
        validateShippableOrder(dto, organizationPublicId);

        if (!dto.getDepartureEta().isBefore(dto.getArrivalEta())) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        LogisticsNode originNode = getActiveNode(dto.getOriginNodePublicId());
        LogisticsNode destinationNode = getActiveNode(dto.getDestinationNodePublicId());

        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }

        Shipment shipment = Shipment.builder()
                .shipmentNumber(generateShipmentNumber(dto))
                .poId(dto.getPoId())
                .purchaseOrderPublicId(dto.getPurchaseOrderPublicId())
                .subPoId(dto.getSubPoId())
                .subPurchaseOrderPublicId(dto.getSubPurchaseOrderPublicId())
                .carrierName(dto.getCarrierName())
                .vehicleNo(dto.getVehicleNo())
                .trackingNo(dto.getTrackingNo())
                .originNodeId(originNode.getId())
                .destinationNodeId(destinationNode.getId())
                .currentNodeId(originNode.getId())
                .departureEta(dto.getDepartureEta())
                .arrivalEta(dto.getArrivalEta())
                .status(ShipmentStatus.READY)
                .temperatureRequired(dto.isTemperatureRequired())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        saveShipmentStatusHistory(
                savedShipment,
                LocalDateTime.now(),
                "출하 생성",
                originNode.getNodeName(),
                originNode.getLatitude(),
                originNode.getLongitude(),
                actorUserPublicId != null ? actorUserPublicId : "SYSTEM"
        );

        shipmentSearchService.saveShipmentDocument(savedShipment);

        outboxEventAppender.append(
                shipmentFactory.createShipmentCreatedEvent(
                        savedShipment,
                        originNode.getPublicId(),
                        destinationNode.getPublicId(),
                        supplyChainContextResolver.fromShipment(savedShipment),
                        actorUserPublicId,
                        organizationPublicId
                )
        );

        return toShipmentResponseDto(savedShipment);
    }

    // 출하 목록 조회
    @Transactional(readOnly = true)
    public Page<ShipmentListResponseDto> getShipments(
            String organizationPublicId,
            String organizationType,
            String userRole,
            Pageable pageable
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Set<Long> myNodeIds = getOrganizationNodeIds(organizationPublicId);

        if (myNodeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Shipment> shipmentPage = shipmentRepository.findByOriginNodeIdInOrDestinationNodeIdIn(
                myNodeIds,
                myNodeIds,
                pageable
        );

        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(shipmentPage.getContent());

        return shipmentPage.map(shipment -> toShipmentListResponseDto(shipment, nodeMap));
    }
    @Transactional(readOnly = true)
    public List<ShipmentMapResponseDto> getShipmentMapData(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Set<Long> myNodeIds = getOrganizationNodeIds(organizationPublicId);
        if (myNodeIds.isEmpty()) {
            return List.of();
        }

        List<ShipmentStatus> activeStatuses = List.of(
                ShipmentStatus.READY,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.DELAYED
        );

        List<Shipment> shipments =
                shipmentRepository.findByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdInOrderByIdDesc(
                        activeStatuses,
                        myNodeIds,
                        activeStatuses,
                        myNodeIds
                );

        if (shipments.isEmpty()) {
            return List.of();
        }

        List<ShipmentCheckpoint> allCheckpoints = shipments.stream()
                .flatMap(shipment -> shipmentCheckpointRepository
                        .findByShipmentIdOrderByActualAtAsc(shipment.getId())
                        .stream())
                .toList();

        Map<Long, LogisticsNode> nodeMap = getShipmentMapNodeMap(shipments, allCheckpoints);

        return shipments.stream()
                .map(shipment -> toShipmentMapResponseDto(shipment, nodeMap))
                .toList();
    }

    // 출하 상세 조회
    @Transactional(readOnly = true)
    public ShipmentResponseDto getShipmentByPublicId(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        return toShipmentResponseDto(shipment);
    }
    public ShipmentResponseDto updateShipment(
            String publicId,
            UpdateShipmentRequestDto dto,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);
        validateShipmentUpdateAuthority(shipment, organizationPublicId, organizationType, userRole);

        validateShipmentUpdatable(shipment);
        validateShipmentUpdateFields(shipment, dto);

        LogisticsNode originNode = resolveUpdatedOriginNode(shipment, dto, organizationPublicId);
        LogisticsNode destinationNode = resolveUpdatedDestinationNode(shipment, dto);

        validateUpdatedShipmentSchedule(dto, shipment);

        Long currentNodeId = shipment.getCurrentNodeId();
        if (!Objects.equals(shipment.getOriginNodeId(), originNode.getId())
                && Objects.equals(shipment.getCurrentNodeId(), shipment.getOriginNodeId())) {
            currentNodeId = originNode.getId();
        }

        shipment.updateShipmentInfo(
                dto.getCarrierName() != null ? dto.getCarrierName() : shipment.getCarrierName(),
                dto.getVehicleNo() != null ? dto.getVehicleNo() : shipment.getVehicleNo(),
                dto.getTrackingNo() != null ? dto.getTrackingNo() : shipment.getTrackingNo(),
                originNode.getId(),
                destinationNode.getId(),
                currentNodeId,
                dto.getDepartureEta() != null ? dto.getDepartureEta() : shipment.getDepartureEta(),
                dto.getArrivalEta() != null ? dto.getArrivalEta() : shipment.getArrivalEta()
        );

        Shipment savedShipment = shipmentRepository.save(shipment);
        shipmentSearchService.saveShipmentDocument(savedShipment);

        return toShipmentResponseDto(savedShipment);
    }
    private void validateShipmentUpdatable(Shipment shipment) {
        if (shipment.getStatus() == ShipmentStatus.ARRIVED
                || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }
    }
    private void validateShipmentUpdateAuthority(
            Shipment shipment,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        boolean isReturnShipment = returnRequestRepository.findByReturnShipmentPublicId(shipment.getPublicId()).isPresent();

        if (isReturnShipment) {
            LogisticsNode originNode = getNode(shipment.getOriginNodeId());

            if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }

            if (!"BUYER".equalsIgnoreCase(organizationType)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }

            return;
        }

        validateCreateShipmentActor(organizationPublicId, organizationType, userRole);
    }

    private void validateShipmentUpdateFields(Shipment shipment, UpdateShipmentRequestDto dto) {
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            return;
        }

        boolean hasNodeChangeRequest =
                (dto.getOriginNodePublicId() != null && !dto.getOriginNodePublicId().isBlank())
                        || (dto.getDestinationNodePublicId() != null && !dto.getDestinationNodePublicId().isBlank());

        boolean hasScheduleChangeRequest =
                dto.getDepartureEta() != null || dto.getArrivalEta() != null;

        if (hasNodeChangeRequest || hasScheduleChangeRequest) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }
    }

    private LogisticsNode resolveUpdatedOriginNode(
            Shipment shipment,
            UpdateShipmentRequestDto dto,
            String organizationPublicId
    ) {
        if (dto.getOriginNodePublicId() == null || dto.getOriginNodePublicId().isBlank()) {
            return getNode(shipment.getOriginNodeId());
        }

        LogisticsNode originNode = getActiveNode(dto.getOriginNodePublicId());
        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }

        return originNode;
    }

    private LogisticsNode resolveUpdatedDestinationNode(
            Shipment shipment,
            UpdateShipmentRequestDto dto
    ) {
        if (dto.getDestinationNodePublicId() == null || dto.getDestinationNodePublicId().isBlank()) {
            return getNode(shipment.getDestinationNodeId());
        }

        return getActiveNode(dto.getDestinationNodePublicId());
    }

    private void validateUpdatedShipmentSchedule(UpdateShipmentRequestDto dto, Shipment shipment) {
        LocalDateTime departureEta = dto.getDepartureEta() != null
                ? dto.getDepartureEta()
                : shipment.getDepartureEta();

        LocalDateTime arrivalEta = dto.getArrivalEta() != null
                ? dto.getArrivalEta()
                : shipment.getArrivalEta();

        if (departureEta != null && arrivalEta != null && !departureEta.isBefore(arrivalEta)) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 출하 위치/상태 추적
    public ShipmentResponseDto trackShipment(
            String publicId,
            TrackShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        validateTrackRequest(dto);
        validateCheckpointAuthority(shipment, dto, organizationPublicId);

        LogisticsNode node = getActiveNode(dto.getNodePublicId());

        if (!isShipmentRelatedNode(shipment, node)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        List<ShipmentCheckpoint> previousCheckpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        EtaCalculationResult previousEtaResult = calculateEta(shipment, previousCheckpoints);

        ShipmentCheckpoint checkpoint = dto.toEntity(shipment.getId(), node.getId());
        shipmentCheckpointRepository.save(checkpoint);

        applyCheckpointToShipment(shipment, dto, node.getId());

        Shipment savedShipment = shipmentRepository.save(shipment);
        shipmentSearchService.saveShipmentDocument(savedShipment);

        if (dto.getCheckpointStatus() == CheckpointStatus.PASSED) {
            saveShipmentStatusHistory(
                    savedShipment,
                    dto.getActualAt(),
                    buildStatusMessage(dto),
                    node.getNodeName(),
                    node.getLatitude(),
                    node.getLongitude(),
                    actorUserPublicId != null ? actorUserPublicId : "SYSTEM"
            );

            List<ShipmentCheckpoint> updatedCheckpoints =
                    shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(savedShipment.getId());

            saveEtaProjectionIfChanged(savedShipment, previousEtaResult, updatedCheckpoints);
            appendShipmentTrackingEvent(savedShipment, dto, actorUserPublicId, organizationPublicId);
        }

        return toShipmentResponseDto(savedShipment);
    }

    // ETA 조회
    @Transactional(readOnly = true)
    public ShipmentEtaResponseDto getShipmentEta(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        List<ShipmentCheckpoint> checkpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        EtaCalculationResult etaResult = calculateEta(shipment, checkpoints);

        return ShipmentEtaResponseDto.builder()
                .publicId(shipment.getPublicId())
                .status(shipment.getStatus())
                .currentNodePublicId(getNodePublicId(shipment.getCurrentNodeId()))
                .destinationNodePublicId(getNodePublicId(shipment.getDestinationNodeId()))
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .estimatedArrivalAt(etaResult.getEstimatedArrivalAt())
                .delayMinutes(etaResult.getDelayMinutes())
                .delayed(etaResult.isDelayed())
                .etaBasis(etaResult.getEtaBasis())
                .lastCheckpointType(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getCheckpointType()
                                : null
                )
                .lastCheckpointAt(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getActualAt()
                                : null
                )
                .lastCheckpointNodePublicId(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? getNodePublicId(etaResult.getLatestPassedCheckpoint().getNodeId())
                                : null
                )
                .build();
    }

    // ETA projection 이력 조회
    @Transactional(readOnly = true)
    public java.util.List<EtaProjectionResponseDto> getEtaProjections(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        java.util.List<EtaProjection> etaProjections =
                etaProjectionRepository.findByShipmentIdOrderByCalculatedAtDesc(shipment.getId());

        return etaProjections.stream()
                .map(EtaProjectionResponseDto::from)
                .toList();
    }

    // 출하 상태 이력 조회
    @Transactional(readOnly = true)
    public java.util.List<ShipmentStatusHistoryResponseDto> getShipmentStatusHistories(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        java.util.List<ShipmentStatusHistory> histories =
                shipmentStatusHistoryRepository.findByShipmentIdOrderByRecordedAtAsc(shipment.getId());

        return histories.stream()
                .map(history -> ShipmentStatusHistoryResponseDto.from(history, shipment.getPublicId()))
                .toList();
    }

    private void validateTrackRequest(TrackShipmentRequestDto dto) {
        if (dto.getCheckpointStatus() == CheckpointStatus.PASSED && dto.getActualAt() == null) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_TRACK_REQUEST);
        }
    }
    private void validateCheckpointAuthority(
            Shipment shipment,
            TrackShipmentRequestDto dto,
            String organizationPublicId
    ) {
        LogisticsNode originNode = getNode(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getNode(shipment.getDestinationNodeId());

        String originOrganizationPublicId = originNode.getOrganizationPublicId();
        String destinationOrganizationPublicId = destinationNode.getOrganizationPublicId();

        if (dto.getCheckpointType() == CheckpointType.DEPARTURE
                || dto.getCheckpointType() == CheckpointType.TRANSIT) {
            if (!originOrganizationPublicId.equals(organizationPublicId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        if (dto.getCheckpointType() == CheckpointType.ARRIVAL
                || dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN) {
            if (!destinationOrganizationPublicId.equals(organizationPublicId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }
        }
    }

    private void applyCheckpointToShipment(Shipment shipment, TrackShipmentRequestDto dto, Long nodeId) {
        if (dto.getCheckpointStatus() != CheckpointStatus.PASSED) {
            return;
        }

        try {
            if (dto.getCheckpointType() == CheckpointType.DEPARTURE) {
                shipment.markInTransit(nodeId, dto.getActualAt());
                return;
            }

            if (dto.getCheckpointType() == CheckpointType.ARRIVAL
                    || dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN) {
                shipment.markArrived(nodeId, dto.getActualAt());
                return;
            }

            shipment.updateCurrentNode(nodeId);
        } catch (IllegalStateException e) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }
    }

    private Shipment getReadableShipment(String publicId, String organizationPublicId) {
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        if (!canReadShipment(shipment, organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND);
        }

        return shipment;
    }

    private boolean canReadShipment(Shipment shipment, String organizationPublicId) {
        LogisticsNode originNode = getNode(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getNode(shipment.getDestinationNodeId());

        return originNode.getOrganizationPublicId().equals(organizationPublicId)
                || destinationNode.getOrganizationPublicId().equals(organizationPublicId);
    }

    private LogisticsNode getActiveNode(String publicId) {
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!node.isActive()) {
            throw new ShipmentException(ShipmentErrorCode.INACTIVE_LOGISTICS_NODE);
        }

        return node;
    }

    private LogisticsNode getNode(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
    }

    private boolean isShipmentRelatedNode(Shipment shipment, LogisticsNode node) {
        return Objects.equals(shipment.getOriginNodeId(), node.getId())
                || Objects.equals(shipment.getDestinationNodeId(), node.getId())
                || Objects.equals(shipment.getCurrentNodeId(), node.getId());
    }

    private Set<Long> getOrganizationNodeIds(String organizationPublicId) {
        return logisticsNodeRepository.findByOrganizationPublicId(organizationPublicId).stream()
                .map(LogisticsNode::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, LogisticsNode> getShipmentNodeMap(Collection<Shipment> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> nodeIds = shipments.stream()
                .flatMap(shipment -> Stream.of(
                        shipment.getOriginNodeId(),
                        shipment.getDestinationNodeId(),
                        shipment.getCurrentNodeId()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(Collectors.toMap(LogisticsNode::getId, node -> node));
    }
    private Map<Long, LogisticsNode> getShipmentMapNodeMap(
            Collection<Shipment> shipments,
            Collection<ShipmentCheckpoint> checkpoints
    ) {
        Set<Long> nodeIds = new HashSet<>();

        if (shipments != null) {
            shipments.stream()
                    .flatMap(shipment -> Stream.of(
                            shipment.getOriginNodeId(),
                            shipment.getDestinationNodeId(),
                            shipment.getCurrentNodeId()
                    ))
                    .filter(Objects::nonNull)
                    .forEach(nodeIds::add);
        }

        if (checkpoints != null) {
            checkpoints.stream()
                    .map(ShipmentCheckpoint::getNodeId)
                    .filter(Objects::nonNull)
                    .forEach(nodeIds::add);
        }

        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(Collectors.toMap(LogisticsNode::getId, node -> node));
    }

    private String getNodePublicId(Long nodeId) {
        if (nodeId == null) {
            return null;
        }

        LogisticsNode node = logisticsNodeService.getLogisticsNodeEntity(nodeId);
        return node.getPublicId();
    }

    private void saveShipmentStatusHistory(
            Shipment shipment,
            LocalDateTime recordedAt,
            String statusMessage,
            String locationText,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            String recordedBy
    ) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipmentId(shipment.getId())
                .statusCode(shipment.getStatus())
                .statusMessage(statusMessage)
                .locationText(locationText)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(recordedAt)
                .recordedBy(recordedBy)
                .build();

        shipmentStatusHistoryRepository.save(history);
    }

    private String buildStatusMessage(TrackShipmentRequestDto dto) {
        if (dto.getCheckpointType() == CheckpointType.DEPARTURE) {
            return "출발 완료";
        }

        if (dto.getCheckpointType() == CheckpointType.TRANSIT) {
            return "경유지 통과";
        }

        if (dto.getCheckpointType() == CheckpointType.ARRIVAL) {
            return "도착 완료";
        }

        if (dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN) {
            return "입고 완료";
        }

        return "출하 상태 변경";
    }

    private ShipmentResponseDto toShipmentResponseDto(Shipment shipment) {
        Map<Long, LogisticsNode> nodeMap = getShipmentNodeMap(List.of(shipment));

        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());

        return ShipmentResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .poId(shipment.getPoId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPoId(shipment.getSubPoId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .originLatitude(originNode != null ? originNode.getLatitude() : null)
                .originLongitude(originNode != null ? originNode.getLongitude() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .destinationLatitude(destinationNode != null ? destinationNode.getLatitude() : null)
                .destinationLongitude(destinationNode != null ? destinationNode.getLongitude() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .currentLatitude(currentNode != null ? currentNode.getLatitude() : null)
                .currentLongitude(currentNode != null ? currentNode.getLongitude() : null)
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .build();
    }

    private ShipmentListResponseDto toShipmentListResponseDto(
            Shipment shipment,
            Map<Long, LogisticsNode> nodeMap
    ) {
        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());

        return ShipmentListResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .carrierName(shipment.getCarrierName())
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .arrivalEta(shipment.getArrivalEta())
                .status(shipment.getStatus())
                .build();
    }
    private ShipmentMapResponseDto toShipmentMapResponseDto(
            Shipment shipment,
            Map<Long, LogisticsNode> nodeMap
    ) {
        LogisticsNode originNode = nodeMap.get(shipment.getOriginNodeId());
        LogisticsNode destinationNode = nodeMap.get(shipment.getDestinationNodeId());
        LogisticsNode currentNode = nodeMap.get(shipment.getCurrentNodeId());

        List<ShipmentCheckpoint> checkpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        EtaCalculationResult etaResult = calculateEta(shipment, checkpoints);

        List<ShipmentMapCheckpointDto> checkpointDtos = checkpoints.stream()
                .map(checkpoint -> toShipmentMapCheckpointDto(checkpoint, nodeMap.get(checkpoint.getNodeId())))
                .toList();

        return ShipmentMapResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .status(shipment.getStatus())
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .originLatitude(originNode != null ? originNode.getLatitude() : null)
                .originLongitude(originNode != null ? originNode.getLongitude() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .destinationLatitude(destinationNode != null ? destinationNode.getLatitude() : null)
                .destinationLongitude(destinationNode != null ? destinationNode.getLongitude() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .currentLatitude(currentNode != null ? currentNode.getLatitude() : null)
                .currentLongitude(currentNode != null ? currentNode.getLongitude() : null)
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .estimatedArrivalAt(etaResult.getEstimatedArrivalAt())
                .delayed(etaResult.isDelayed())
                .delayMinutes(etaResult.getDelayMinutes())
                .etaBasis(etaResult.getEtaBasis())
                .lastCheckpointType(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getCheckpointType()
                                : null
                )
                .lastCheckpointAt(
                        etaResult.getLatestPassedCheckpoint() != null
                                ? etaResult.getLatestPassedCheckpoint().getActualAt()
                                : null
                )
                .checkpoints(checkpointDtos)
                .build();
    }
    private ShipmentMapCheckpointDto toShipmentMapCheckpointDto(
            ShipmentCheckpoint checkpoint,
            LogisticsNode node
    ) {
        return ShipmentMapCheckpointDto.builder()
                .nodePublicId(node != null ? node.getPublicId() : null)
                .nodeName(node != null ? node.getNodeName() : null)
                .nodeCode(node != null ? node.getNodeCode() : null)
                .checkpointType(checkpoint.getCheckpointType())
                .checkpointStatus(checkpoint.getCheckpointStatus())
                .plannedAt(checkpoint.getPlannedAt())
                .actualAt(checkpoint.getActualAt())
                .latitude(node != null ? node.getLatitude() : null)
                .longitude(node != null ? node.getLongitude() : null)
                .note(checkpoint.getNote())
                .build();
    }

    private void validateShipmentActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        if (ADMIN_ORGANIZATION_TYPE.equals(organizationType) || ADMIN_ROLE.equals(userRole)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    private void validateCreateShipmentActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        if (!"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_CREATION_NOT_ALLOWED);
        }
    }
    private void validateShippableOrder(CreateShipmentRequestDto dto, String organizationPublicId) {
        if (dto.getSubPoId() != null) {
            validateSubPurchaseOrderForShipment(dto.getSubPoId(), organizationPublicId);
            return;
        }

        if (dto.getPoId() == null) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        validatePurchaseOrderForShipment(dto.getPoId(), organizationPublicId);
    }

    private void validatePurchaseOrderForShipment(Long poId, String organizationPublicId) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!purchaseOrder.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        if (!isShippablePurchaseOrderStatus(purchaseOrder.getPoStatus())) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_ORDER_STATUS_NOT_ALLOWED);
        }
    }
    private String generateShipmentNumber(CreateShipmentRequestDto dto) {
        String orderNumber = resolveShipmentOrderNumber(dto);
        String prefix = "SHIP-" + orderNumber + "-";

        String lastShipmentNumber = shipmentRepository
                .findTopByShipmentNumberStartingWithOrderByShipmentNumberDesc(prefix)
                .map(Shipment::getShipmentNumber)
                .orElse(null);

        int nextSequence = extractNextShipmentSequence(lastShipmentNumber);

        String candidate = prefix + String.format("%03d", nextSequence);
        while (shipmentRepository.existsByShipmentNumber(candidate)) {
            nextSequence++;
            candidate = prefix + String.format("%03d", nextSequence);
        }

        return candidate;
    }

    private String resolveShipmentOrderNumber(CreateShipmentRequestDto dto) {
        if (dto.getSubPoId() != null) {
            SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(dto.getSubPoId())
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
            return subPurchaseOrder.getSubPoNumber();
        }

        if (dto.getPoId() != null) {
            SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(dto.getPoId())
                    .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));
            return purchaseOrder.getPoNumber();
        }

        throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
    }

    private int extractNextShipmentSequence(String lastShipmentNumber) {
        if (lastShipmentNumber == null || lastShipmentNumber.isBlank()) {
            return 1;
        }

        int lastHyphenIndex = lastShipmentNumber.lastIndexOf('-');
        if (lastHyphenIndex < 0 || lastHyphenIndex == lastShipmentNumber.length() - 1) {
            return 1;
        }

        String lastSequenceText = lastShipmentNumber.substring(lastHyphenIndex + 1);
        try {
            return Integer.parseInt(lastSequenceText) + 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void validateSubPurchaseOrderForShipment(Long subPoId, String organizationPublicId) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(subPoId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!subPurchaseOrder.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        if (!isShippableSubPurchaseOrderStatus(subPurchaseOrder.getSubPoStatus())) {
            throw new ShipmentException(ShipmentErrorCode.SHIPMENT_ORDER_STATUS_NOT_ALLOWED);
        }
    }

    private boolean isShippablePurchaseOrderStatus(PoStatus poStatus) {
        return poStatus == PoStatus.PARTIALLY_CONFIRMED
                || poStatus == PoStatus.CONFIRMED;
    }

    private boolean isShippableSubPurchaseOrderStatus(SubPoStatus subPoStatus) {
        return subPoStatus == SubPoStatus.PARTIALLY_CONFIRMED
                || subPoStatus == SubPoStatus.CONFIRMED;
    }

    @Getter
    @AllArgsConstructor
    private static class EtaCalculationResult {
        private LocalDateTime estimatedArrivalAt;
        private Long delayMinutes;
        private boolean delayed;
        private EtaBasis etaBasis;
        private ShipmentCheckpoint latestPassedCheckpoint;
    }

    private EtaCalculationResult calculateEta(Shipment shipment, java.util.List<ShipmentCheckpoint> checkpoints) {
        ShipmentCheckpoint latestPassedCheckpoint = checkpoints.stream()
                .filter(checkpoint -> checkpoint.getCheckpointStatus() == CheckpointStatus.PASSED)
                .filter(checkpoint -> checkpoint.getActualAt() != null)
                .reduce((first, second) -> second)
                .orElse(null);

        LocalDateTime estimatedArrivalAt;
        EtaBasis etaBasis;
        boolean delayed = false;
        long delayMinutes = 0L;

        if (shipment.getStatus() == ShipmentStatus.ARRIVED && shipment.getActualArrivedAt() != null) {
            estimatedArrivalAt = shipment.getActualArrivedAt();
            etaBasis = EtaBasis.ARRIVED;

            if (shipment.getArrivalEta() != null && shipment.getActualArrivedAt().isAfter(shipment.getArrivalEta())) {
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), shipment.getActualArrivedAt()).toMinutes();
            }
        } else if (shipment.getActualDepartedAt() != null
                && shipment.getDepartureEta() != null
                && shipment.getArrivalEta() != null) {

            Duration plannedDuration = Duration.between(shipment.getDepartureEta(), shipment.getArrivalEta());
            estimatedArrivalAt = shipment.getActualDepartedAt().plus(plannedDuration);
            etaBasis = EtaBasis.ACTUAL_TRACKING;

            if (estimatedArrivalAt.isAfter(shipment.getArrivalEta())) {
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), estimatedArrivalAt).toMinutes();
            }
        } else {
            estimatedArrivalAt = shipment.getArrivalEta();
            etaBasis = EtaBasis.SCHEDULED;

            if (shipment.getArrivalEta() != null
                    && shipment.getStatus() != ShipmentStatus.ARRIVED
                    && LocalDateTime.now().isAfter(shipment.getArrivalEta())) {
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), LocalDateTime.now()).toMinutes();
            }
        }

        return new EtaCalculationResult(
                estimatedArrivalAt,
                delayMinutes,
                delayed,
                etaBasis,
                latestPassedCheckpoint
        );
    }

    private void saveEtaProjectionIfChanged(
            Shipment shipment,
            EtaCalculationResult previousEtaResult,
            java.util.List<ShipmentCheckpoint> updatedCheckpoints
    ) {
        EtaCalculationResult currentEtaResult = calculateEta(shipment, updatedCheckpoints);

        LocalDateTime previousEta =
                previousEtaResult != null ? previousEtaResult.getEstimatedArrivalAt() : null;
        LocalDateTime projectedEta = currentEtaResult.getEstimatedArrivalAt();

        if ((previousEta == null && projectedEta == null)
                || (previousEta != null && previousEta.equals(projectedEta))) {
            return;
        }

        EtaProjection etaProjection = EtaProjection.builder()
                .shipmentId(shipment.getId())
                .riskEventId(null)
                .previousEta(previousEta)
                .projectedEta(projectedEta)
                .delayMinutes(currentEtaResult.getDelayMinutes())
                .calculatedAt(LocalDateTime.now())
                .build();

        etaProjectionRepository.save(etaProjection);
    }

    private void appendShipmentTrackingEvent(
            Shipment shipment,
            TrackShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        String eventType = resolveShipmentTrackingEventType(dto);
        if (eventType == null) {
            return;
        }

        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_SHIPMENT,
                        eventType,
                        AggregateType.SHIPMENT,
                        shipment.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        supplyChainContextResolver.fromShipment(shipment),
                        supplyDomainEventFactory.payload(
                                shipment.getPublicId(),
                                shipment.getShipmentNumber(),
                                shipment.getStatus().name(),
                                resolveShipmentTrackingEventName(eventType),
                                resolveShipmentTrackingDescription(eventType),
                                null
                        )
                )
        );
    }

    private String resolveShipmentTrackingEventType(TrackShipmentRequestDto dto) {
        if (dto.getCheckpointType() == CheckpointType.DEPARTURE) {
            return EventTypes.SHIPMENT_DEPARTED;
        }
        if (dto.getCheckpointType() == CheckpointType.ARRIVAL
                || dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN) {
            return EventTypes.SHIPMENT_ARRIVED;
        }
        return null;
    }

    private String resolveShipmentTrackingEventName(String eventType) {
        if (EventTypes.SHIPMENT_DEPARTED.equals(eventType)) {
            return "출하 출발";
        }
        return "출하 도착";
    }

    private String resolveShipmentTrackingDescription(String eventType) {
        if (EventTypes.SHIPMENT_DEPARTED.equals(eventType)) {
            return "출하 출발 시";
        }
        return "출하 도착 시";
    }
}
