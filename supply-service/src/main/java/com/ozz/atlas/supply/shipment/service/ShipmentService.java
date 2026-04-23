package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.kafka.shipment.ShipmentFactory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
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

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentCheckpointRepository shipmentCheckpointRepository,
            ShipmentStatusHistoryRepository shipmentStatusHistoryRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            LogisticsNodeService logisticsNodeService,
            ShipmentSearchService shipmentSearchService,
            EtaProjectionRepository etaProjectionRepository,
            OutboxEventAppender outboxEventAppender,
            ShipmentFactory shipmentFactory
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
    }

    // 출하 생성
    public ShipmentResponseDto createShipment(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateShipmentActor(organizationPublicId, organizationType, userRole);

        if (!dto.getDepartureEta().isBefore(dto.getArrivalEta())) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }

        LogisticsNode originNode = getActiveNode(dto.getOriginNodePublicId());
        LogisticsNode destinationNode = getActiveNode(dto.getDestinationNodePublicId());

        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)
                && !destinationNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        Shipment shipment = Shipment.builder()
                .shipmentNumber(dto.getShipmentNumber())
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
                .originLatitude(originNode != null ? originNode.getLatitude() : null)
                .originLongitude(originNode != null ? originNode.getLongitude() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationLatitude(destinationNode != null ? destinationNode.getLatitude() : null)
                .destinationLongitude(destinationNode != null ? destinationNode.getLongitude() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
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
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .arrivalEta(shipment.getArrivalEta())
                .status(shipment.getStatus())
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
}
