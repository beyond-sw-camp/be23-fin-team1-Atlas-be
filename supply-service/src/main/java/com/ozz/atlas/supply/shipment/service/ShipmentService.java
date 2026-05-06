package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.shipment.domain.CheckpointStatus;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.EtaProjection;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentCheckpoint;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import com.ozz.atlas.supply.shipment.dtos.*;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.EtaProjectionRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentStatusHistoryRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository shipmentCheckpointRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final LogisticsNodeService logisticsNodeService;
    private final ShipmentSearchService shipmentSearchService;
    private final EtaProjectionRepository etaProjectionRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final ReturnRequestRepository returnRequestRepository;
    private final SupplyChainContextResolver supplyChainContextResolver;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final ShipmentAuthorizationService shipmentAuthorizationService;
    private final ShipmentMapService shipmentMapService;
    private final ShipmentStatusService shipmentStatusService;
    private final ShipmentCreateService shipmentCreateService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentEtaCalculator shipmentEtaCalculator;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentCheckpointRepository shipmentCheckpointRepository,
            ShipmentStatusHistoryRepository shipmentStatusHistoryRepository,
            LogisticsNodeRepository logisticsNodeRepository,
            LogisticsNodeService logisticsNodeService,
            ShipmentSearchService shipmentSearchService,
            EtaProjectionRepository etaProjectionRepository,
            OutboxEventAppender outboxEventAppender,
            ReturnRequestRepository returnRequestRepository,
            SupplyChainContextResolver supplyChainContextResolver,
            SupplyDomainEventFactory supplyDomainEventFactory,
            ShipmentAuthorizationService shipmentAuthorizationService,
            ShipmentMapService shipmentMapService,
            ShipmentStatusService shipmentStatusService,
            ShipmentCreateService shipmentCreateService,
            ShipmentMapper shipmentMapper,
            ShipmentEtaCalculator shipmentEtaCalculator
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentCheckpointRepository = shipmentCheckpointRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.logisticsNodeService = logisticsNodeService;
        this.shipmentSearchService = shipmentSearchService;
        this.etaProjectionRepository = etaProjectionRepository;
        this.outboxEventAppender = outboxEventAppender;
        this.returnRequestRepository = returnRequestRepository;
        this.supplyChainContextResolver = supplyChainContextResolver;
        this.supplyDomainEventFactory = supplyDomainEventFactory;
        this.shipmentAuthorizationService = shipmentAuthorizationService;
        this.shipmentMapService = shipmentMapService;
        this.shipmentStatusService = shipmentStatusService;
        this.shipmentCreateService = shipmentCreateService;
        this.shipmentMapper = shipmentMapper;
        this.shipmentEtaCalculator = shipmentEtaCalculator;
    }

    // 출하 생성
    public ShipmentResponseDto createShipment(
            CreateShipmentRequestDto dto,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentCreateService.createShipment(
                dto,
                actorUserPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );
    }

    @Transactional(readOnly = true)
    public Page<ShipmentListResponseDto> getShipments(
            String organizationPublicId,
            String organizationType,
            String userRole,
            Pageable pageable
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Set<Long> myNodeIds = getOrganizationNodeIds(organizationPublicId);

        if (myNodeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Shipment> shipmentPage = shipmentRepository.findByOriginNodeIdInOrDestinationNodeIdIn(
                myNodeIds,
                myNodeIds,
                pageable
        );

        return shipmentPage.map(shipment -> shipmentMapper.toShipmentListResponseDto(shipment, organizationPublicId));
    }

    @Transactional(readOnly = true)
    public List<ShipmentCreatableOrderDto> getCreatableOrders(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentCreateService.getCreatableOrders(
                organizationPublicId,
                organizationType,
                userRole
        );
    }

    @Transactional(readOnly = true)
    public List<ShipmentMapResponseDto> getShipmentMapData(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentMapService.getShipmentMapData(
                organizationPublicId,
                organizationType,
                userRole
        );
    }

    // 출하 상세 조회
    @Transactional(readOnly = true)
    public ShipmentResponseDto getShipmentByPublicId(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        return shipmentMapper.toShipmentResponseDto(shipment, organizationPublicId);
    }

    public ShipmentResponseDto updateShipment(
            String publicId,
            UpdateShipmentRequestDto dto,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);
        validateShipmentUpdateAuthority(shipment, organizationPublicId, organizationType, userRole);

        validateShipmentReadyForScheduleUpdate(shipment, dto);

        shipment.updateShipmentInfo(
                shipment.getCarrierName(),
                shipment.getVehicleNo(),
                shipment.getTrackingNo(),
                shipment.getOriginNodeId(),
                shipment.getDestinationNodeId(),
                shipment.getCurrentNodeId(),
                dto.getDepartureEta(),
                shipment.getArrivalEta()
        );

        shipment.updateShipmentOptions(
                resolveUpdateFlag(dto.getTemperatureRequired(), shipment.isTemperatureRequired()),
                resolveUpdateFlag(dto.getSealedPackagingRequired(), shipment.isSealedPackagingRequired()),
                resolveUpdateFlag(dto.getFragile(), shipment.isFragile())
        );

        Shipment savedShipment = shipmentRepository.save(shipment);
        shipmentSearchService.saveShipmentDocument(savedShipment);

        return shipmentMapper.toShipmentResponseDto(savedShipment, organizationPublicId);
    }

    public ShipmentResponseDto startShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentStatusService.startShipment(
                publicId,
                actorUserPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );
    }

    public ShipmentResponseDto arriveShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentStatusService.arriveShipment(
                publicId,
                actorUserPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );
    }

    public ShipmentResponseDto cancelShipment(
            String publicId,
            String actorUserPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        return shipmentStatusService.cancelShipment(
                publicId,
                actorUserPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );
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

        shipmentAuthorizationService.validateCreateShipmentActor(organizationPublicId, organizationType, userRole);
    }

    private void validateShipmentReadyForScheduleUpdate(Shipment shipment, UpdateShipmentRequestDto dto) {
        if (shipment.getStatus() != ShipmentStatus.READY) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }

        if (dto.getDepartureEta() == null) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean resolveUpdateFlag(Boolean value, boolean currentValue) {
        return value != null ? value : currentValue;
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
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        validateTrackRequest(dto);
        validateCheckpointAuthority(shipment, dto, organizationPublicId);

        LogisticsNode node = getActiveNode(dto.getNodePublicId());

        if (!isShipmentRelatedNode(shipment, node)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        List<ShipmentCheckpoint> previousCheckpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        ShipmentEtaCalculator.Result previousEtaResult = shipmentEtaCalculator.calculate(shipment, previousCheckpoints);

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

        return shipmentMapper.toShipmentResponseDto(savedShipment, organizationPublicId);
    }

    // ETA 조회
    @Transactional(readOnly = true)
    public ShipmentEtaResponseDto getShipmentEta(
            String publicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

        Shipment shipment = getReadableShipment(publicId, organizationPublicId);

        List<ShipmentCheckpoint> checkpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        ShipmentEtaCalculator.Result etaResult = shipmentEtaCalculator.calculate(shipment, checkpoints);

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
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

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
        shipmentAuthorizationService.validateShipmentActor(organizationPublicId, organizationType, userRole);

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

        // 출발/도착 상태 변경은 /start, /arrive API에서만 처리합니다.
        // /track은 배송 중 경유 위치 기록용으로만 사용합니다.
        if (dto.getCheckpointType() != CheckpointType.TRANSIT) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_TRACK_REQUEST);
        }
    }

    private void validateCheckpointAuthority(
            Shipment shipment,
            TrackShipmentRequestDto dto,
            String organizationPublicId
    ) {
        LogisticsNode originNode = getNode(shipment.getOriginNodeId());

        // /track은 배송 중 위치 업데이트 성격이므로 보내는 쪽만 등록할 수 있게 제한합니다.
        if (!originNode.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }

    private void applyCheckpointToShipment(Shipment shipment, TrackShipmentRequestDto dto, Long nodeId) {
        if (dto.getCheckpointStatus() != CheckpointStatus.PASSED) {
            return;
        }

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT
                && shipment.getStatus() != ShipmentStatus.DELAYED) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION);
        }

        shipment.updateCurrentNode(nodeId);
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

    private LogisticsNode getActiveNode(Long nodeId) {
        LogisticsNode node = logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.INVALID_INPUT_VALUE));

        if (!node.isActive()) {
            throw new ShipmentException(ShipmentErrorCode.INACTIVE_LOGISTICS_NODE);
        }

        return node;
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
        return "배송 위치 업데이트";
    }

    private void saveEtaProjectionIfChanged(
            Shipment shipment,
            ShipmentEtaCalculator.Result previousEtaResult,
            java.util.List<ShipmentCheckpoint> updatedCheckpoints
    ) {
        ShipmentEtaCalculator.Result currentEtaResult = shipmentEtaCalculator.calculate(shipment, updatedCheckpoints);

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
        if (dto.getCheckpointType() == CheckpointType.ARRIVAL) {
            return EventTypes.SHIPMENT_ARRIVED;
        }
        if (dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN) {
            return EventTypes.SHIPMENT_COMPLETED;
        }
        return null;
    }

    private String resolveShipmentTrackingEventName(String eventType) {
        if (EventTypes.SHIPMENT_DEPARTED.equals(eventType)) {
            return "출하 출발";
        }
        if (EventTypes.SHIPMENT_COMPLETED.equals(eventType)) {
            return "출하 완료";
        }
        return "출하 도착";
    }

    private String resolveShipmentTrackingDescription(String eventType) {
        if (EventTypes.SHIPMENT_DEPARTED.equals(eventType)) {
            return "출하 출발 시";
        }
        if (EventTypes.SHIPMENT_COMPLETED.equals(eventType)) {
            return "출하 완료 시";
        }
        return "출하 도착 시";
    }
}
