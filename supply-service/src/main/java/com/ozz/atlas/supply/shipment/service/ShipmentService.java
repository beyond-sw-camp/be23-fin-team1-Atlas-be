package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import com.ozz.atlas.supply.shipment.domain.*;
import com.ozz.atlas.supply.shipment.dtos.*;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository shipmentCheckpointRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final LogisticsNodeService logisticsNodeService;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentCheckpointRepository shipmentCheckpointRepository, ShipmentStatusHistoryRepository shipmentStatusHistoryRepository, LogisticsNodeService logisticsNodeService) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentCheckpointRepository = shipmentCheckpointRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.logisticsNodeService = logisticsNodeService;
    }

//    출하 생성
//    출발지/도착지 물류거점 존재 여부 검증(publicId) 뒤 출하 저장
//    생성 직후 내부 id를 shipment에 저장 후 READY상태 이력 + 출발지 위치 정보 기록
    @Transactional
    public ShipmentResponseDto createShipment(CreateShipmentRequestDto dto){
        LogisticsNode originNode = logisticsNodeService.getLogisticsNodeEntityByPublicId(dto.getOriginNodePublicId());
        LogisticsNode destinationNode = logisticsNodeService.getLogisticsNodeEntityByPublicId(dto.getDestinationNodePublicId());

        Shipment shipment = Shipment.builder()
                .shipmentNumber(dto.getShipmentNumber())
                .poId(dto.getPoId())
                .subPoId(dto.getSubPoId())
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
                "SYSTEM"
        );
        return toShipmentResponseDto(savedShipment);
    }

//    출하 목록 조회
//    1. shipment page를 가져온다.
//    2. page안의 모든 node id를 모은다.
//    3. logistics에서 한번에 조회
//    4. map을 사용해 dto 생성
    @Transactional(readOnly = true)
    public Page<ShipmentListResponseDto> getShipments(Pageable pageable) {
        Page<Shipment> shipmentPage = shipmentRepository.findAll(pageable);

        Set<Long> nodeIds = shipmentPage.getContent().stream()
                .flatMap(shipment -> java.util.stream.Stream.of(
                        shipment.getDestinationNodeId(),
                        shipment.getCurrentNodeId()
                ))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> nodePublicIdMap = logisticsNodeService.getNodePublicIdMap(nodeIds);

        return shipmentPage.map(shipment -> toShipmentListResponseDto(shipment, nodePublicIdMap));
    }

//    출하 상세 조회
    @Transactional(readOnly = true)
    public ShipmentResponseDto getShipmentByPublicId(String publicId){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return toShipmentResponseDto(shipment);
    }

//    track 생성
//    shipment publicId와 node publicId로 입력 받고
//    내부에서 shipment id와 node id를 사용해 checkpoint+statusHistory 기록
    public ShipmentResponseDto trackShipment(String publicId, TrackShipmentRequestDto dto){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        validateTrackRequest(dto);

        LogisticsNode node = logisticsNodeService.getLogisticsNodeEntityByPublicId(dto.getNodePublicId());

        ShipmentCheckpoint checkpoint = dto.toEntity(shipment.getId(), node.getId());
        shipmentCheckpointRepository.save(checkpoint);

//        PASSED 체크포인트만 shipment 현재상태에 반영
//        출발-IN_TRANSIT / 도착,입고-ARRIVED, 그외는 현재 노드만 갱신
        applyCheckpointToShipment(shipment, dto, node.getId());

        Shipment savedShipment = shipmentRepository.save(shipment);

        if (dto.getCheckpointStatus() == CheckpointStatus.PASSED) {
            saveShipmentStatusHistory(
                    savedShipment,
                    dto.getActualAt(),
                    buildStatusMessage(dto),
                    node.getNodeName(),
                    node.getLatitude(),
                    node.getLongitude(),
                    "SYSTEM"
            );
        }
        return toShipmentResponseDto(savedShipment);
    }

//    service 내부 검증
//    PASSED 체크포인트는 실제 처리 시각(actualAt)이 필요
    private void validateTrackRequest(TrackShipmentRequestDto dto){
        if (dto.getCheckpointStatus() == CheckpointStatus.PASSED && dto.getActualAt() == null){
            throw new ShipmentException(ShipmentErrorCode.INVALID_TRACK_REQUEST);
        }
    }

//    service 내부 상태 반영
//    PASSED 체크포인트만 shipment 현재 상태에 반영
//    DEPARTURE : IN_TRANSIT / ARRIVAL, WAREHOUSE_IN : ARRIVED / 그외 : 현재 노드만 갱신
    private  void applyCheckpointToShipment(Shipment shipment, TrackShipmentRequestDto dto, Long nodeId){
        if (dto.getCheckpointStatus() != CheckpointStatus.PASSED){
            return;
        }
        if (dto.getCheckpointType() == CheckpointType.DEPARTURE){
            shipment.markInTransit(nodeId, dto.getActualAt());
            return;
        }
        if (dto.getCheckpointType() == CheckpointType.ARRIVAL || dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN){
            shipment.markArrived(nodeId, dto.getActualAt());
            return;
        }
        shipment.updateCurrentNode(nodeId);
    }

//    ETA
//    shipment상태 + checkpoint 기준 ETA계산
    @Transactional(readOnly = true)
    public ShipmentEtaResponseDto getShipmentEta(String publicId){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        List<ShipmentCheckpoint> checkpoints =
                shipmentCheckpointRepository.findByShipmentIdOrderByActualAtAsc(shipment.getId());

        ShipmentCheckpoint latestPassedCheckpoint = checkpoints.stream()
                .filter(checkpoint -> checkpoint.getCheckpointStatus() == CheckpointStatus.PASSED)
                .filter(checkpoint -> checkpoint.getActualAt() != null)
                .reduce((first, second) -> second)
                .orElse(null);

        LocalDateTime estimatedArrivalAt;
        EtaBasis etaBasis;
        boolean delayed = false;
        long delayMinutes = 0L;

        if (shipment.getStatus() == ShipmentStatus.ARRIVED && shipment.getActualArrivedAt() != null){
            estimatedArrivalAt = shipment.getActualArrivedAt();
            etaBasis = EtaBasis.ARRIVED;

            if (shipment.getArrivalEta() != null && shipment.getActualArrivedAt().isAfter(shipment.getArrivalEta())){
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), shipment.getActualArrivedAt()).toMinutes();
            }
        } else if (shipment.getActualDepartedAt() != null
                && shipment.getDepartureEta() != null
                && shipment.getArrivalEta() != null){

            Duration plannedDuration = Duration.between(shipment.getDepartureEta(), shipment.getArrivalEta());
            estimatedArrivalAt = shipment.getActualDepartedAt().plus(plannedDuration);
            etaBasis = EtaBasis.ACTUAL_TRACKING;

            if (estimatedArrivalAt.isAfter(shipment.getArrivalEta())){
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), estimatedArrivalAt).toMinutes();
            }
        } else {
            estimatedArrivalAt = shipment.getArrivalEta();
            etaBasis = EtaBasis.SCHEDULED;

            if (shipment.getArrivalEta() != null
                    && shipment.getStatus() != ShipmentStatus.ARRIVED
                    && LocalDateTime.now().isAfter(shipment.getArrivalEta())){
                delayed = true;
                delayMinutes = Duration.between(shipment.getArrivalEta(), LocalDateTime.now()).toMinutes();
            }
        }
        return ShipmentEtaResponseDto.builder()
                .publicId(shipment.getPublicId())
                .status(shipment.getStatus())
                .currentNodePublicId(getNodePublicId(shipment.getCurrentNodeId()))
                .destinationNodePublicId(getNodePublicId(shipment.getDestinationNodeId()))
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .estimatedArrivalAt(estimatedArrivalAt)
                .delayMinutes(delayMinutes)
                .delayed(delayed)
                .etaBasis(etaBasis)
                .lastCheckpointType(latestPassedCheckpoint != null ? latestPassedCheckpoint.getCheckpointType() : null)
                .lastCheckpointAt(latestPassedCheckpoint != null ? latestPassedCheckpoint.getActualAt() : null)
                .lastCheckpointNodePublicId(
                        latestPassedCheckpoint != null
                                ? getNodePublicId(latestPassedCheckpoint.getNodeId())
                                : null
                )
                .build();
    }

//    출하 / 트랙 상태 이력 저장
//    shipment 상태 이력은 내부 shipment id로 저장, 위치 정보는 Logistics node의 이름과 좌표 사용
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

//    출하 / 트랙 상태 메시지 생성
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

//    statusHistory 저장
    @Transactional(readOnly = true)
    public List<ShipmentStatusHistoryResponseDto> getShipmentStatusHistories(String publicId){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        List<ShipmentStatusHistory> histories =
                shipmentStatusHistoryRepository.findByShipmentIdOrderByRecordedAtAsc(shipment.getId());

        return histories.stream().map(history -> ShipmentStatusHistoryResponseDto.from(history, shipment.getPublicId())).toList();
    }

//    (shipment내부)nodeId -> (응답용)publicId 변환
//    상세 조회 / ETA 조회에 사용
    private String getNodePublicId(Long nodeId){
        if (nodeId == null){
            return null;
        }

        LogisticsNode node = logisticsNodeService.getLogisticsNodeEntity(nodeId);
        return node.getPublicId();
    }

//    상세 조회용 dto
    private ShipmentResponseDto toShipmentResponseDto(Shipment shipment) {
        return ShipmentResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .poId(shipment.getPoId())
                .subPoId(shipment.getSubPoId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originNodePublicId(getNodePublicId(shipment.getOriginNodeId()))
                .destinationNodePublicId(getNodePublicId(shipment.getDestinationNodeId()))
                .currentNodePublicId(getNodePublicId(shipment.getCurrentNodeId()))
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .build();
    }

//    목록 조회용 dto
    private ShipmentListResponseDto toShipmentListResponseDto(
            Shipment shipment, Map<Long, String> nodePublicIdMap) {
        return ShipmentListResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .carrierName(shipment.getCarrierName())
                .destinationNodePublicId(nodePublicIdMap.get(shipment.getDestinationNodeId()))
                .currentNodePublicId(nodePublicIdMap.get(shipment.getCurrentNodeId()))
                .arrivalEta(shipment.getArrivalEta())
                .status(shipment.getStatus())
                .build();
    }
}
