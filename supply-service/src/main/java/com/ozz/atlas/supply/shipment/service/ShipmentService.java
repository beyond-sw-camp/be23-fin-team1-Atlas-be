package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.shipment.domain.*;
import com.ozz.atlas.supply.shipment.dtos.*;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentCheckpointRepository shipmentCheckpointRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentCheckpointRepository shipmentCheckpointRepository) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentCheckpointRepository = shipmentCheckpointRepository;
    }

//    출하 생성
    @Transactional
    public ShipmentResponseDto createShipment(CreateShipmentRequestDto dto){
        Shipment savedShipment = shipmentRepository.save(dto.toEntity());
        return ShipmentResponseDto.from(savedShipment);
    }

//    출하 목록 조회
    @Transactional(readOnly = true)
    public Page<ShipmentListResponseDto> getShipments(Pageable pageable) {
        Page<Shipment> shipmentPage = shipmentRepository.findAll(pageable);
        return shipmentPage.map(ShipmentListResponseDto::from);
    }

//    출하 상세 조회
    @Transactional(readOnly = true)
    public ShipmentResponseDto getShipmentById(Long id){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return ShipmentResponseDto.from(shipment);
    }

//    track 생성
    public ShipmentResponseDto trackShipment(Long id, TrackShipmentRequestDto dto){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        validateTrackRequest(dto);

        ShipmentCheckpoint checkpoint = dto.toEntity(id);
        shipmentCheckpointRepository.save(checkpoint);

        applyCheckpointToShipment(shipment, dto);

        return ShipmentResponseDto.from(shipmentRepository.save(shipment));
    }

//    service 내부 검증
    private void validateTrackRequest(TrackShipmentRequestDto dto){
        if (dto.getCheckpointStatus() == CheckpointStatus.PASSED && dto.getActualAt() == null){
            throw new ShipmentException(ShipmentErrorCode.INVALID_TRACK_REQUEST);
        }
    }

//    service 내부 상태 반영
    private  void applyCheckpointToShipment(Shipment shipment, TrackShipmentRequestDto dto){
        if (dto.getCheckpointStatus() != CheckpointStatus.PASSED){
            return;
        }
        if (dto.getCheckpointType() == CheckpointType.DEPARTURE){
            shipment.markInTransit(dto.getNodeId(), dto.getActualAt());
            return;
        }
        if (dto.getCheckpointType() == CheckpointType.ARRIVAL || dto.getCheckpointType() == CheckpointType.WAREHOUSE_IN){
            shipment.markArrived(dto.getNodeId(), dto.getActualAt());
            return;
        }
        shipment.updateCurrentNode(dto.getNodeId());
    }

//    ETA - 단순 버전
//    실제 이동 상황 반영X - 계획 도착 시간 반영 / 계획 ETA 조회
//    @Transactional(readOnly = true)
//    public ShipmentEtaResponseDto getShipmentEta(Long id){
//        Shipment shipment = shipmentRepository.findById(id)
//                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));
//
//        boolean delayed = shipment.getStatus() != ShipmentStatus.ARRIVED
//                && shipment.getArrivalEta() != null
//                && LocalDateTime.now().isAfter(shipment.getArrivalEta());
//
//        return ShipmentEtaResponseDto.builder()
//                .id(shipment.getId())
//                .status(shipment.getStatus())
//                .currentNodeId(shipment.getCurrentNodeId())
//                .destinationNodeId(shipment.getDestinationNodeId())
//                .arrivalEta(shipment.getArrivalEta())
//                .delayed(delayed)
//                .build();
//    }

//    ETA - 확장 버전
//    checkpoint + 실제 출발 시간 반영
    @Transactional(readOnly = true)
    public ShipmentEtaResponseDto getShipmentEta(Long id){
        Shipment shipment = shipmentRepository.findById(id)
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
                .id(shipment.getId())
                .status(shipment.getStatus())
                .currentNodeId(shipment.getCurrentNodeId())
                .destinationNodeId(shipment.getDestinationNodeId())
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
                .lastCheckpointNodeId(latestPassedCheckpoint != null ? latestPassedCheckpoint.getNodeId() : null)
                .build();
    }
}
