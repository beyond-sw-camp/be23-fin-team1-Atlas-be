package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.shipment.domain.*;
import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.dtos.TrackShipmentRequestDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.ShipmentCheckpointRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

//    출하 목록 조회
    public List<ShipmentListResponseDto> getShipments(){
        return shipmentRepository.findAll()
                .stream()
                .map(this::toListResponseDto)
                .toList();
    }

//    출하 상세 조회
    public ShipmentResponseDto getShipmentByPublicId(String publicId){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return toResponseDto(shipment);
    }
    @Transactional
    public ShipmentResponseDto createShipment(CreateShipmentRequestDto dto){
        Shipment shipment = Shipment.builder()
                .shipmentNumber(dto.getShipmentNumber())
                .poId(dto.getPoId())
                .subPoId(dto.getSubPoId())
                .carrierName(dto.getCarrierName())
                .vehicleNo(dto.getVehicleNo())
                .trackingNo(dto.getTrackingNo())
                .originNodeId(dto.getOriginNodeId())
                .destinationNodeId(dto.getDestinationNodeId())
                .currentNodeId(dto.getOriginNodeId())
                .departureEta(dto.getDepartureEta())
                .arrivalEta(dto.getArrivalEta())
                .status(ShipmentStatus.READY)
                .temperatureRequired(dto.isTemperatureRequired())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        return toResponseDto(savedShipment);
    }

//    출하 목록 조립
    private ShipmentListResponseDto toListResponseDto(Shipment shipment){
        return ShipmentListResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .carrierName(shipment.getCarrierName())
                .currentNodeId(shipment.getCurrentNodeId())
                .destinationNodeId(shipment.getDestinationNodeId())
                .arrivalEta(shipment.getArrivalEta())
                .status(shipment.getStatus())
                .build();
    }

//    출하 상세 조립
    private ShipmentResponseDto toResponseDto(Shipment shipment) {
        return ShipmentResponseDto.builder()
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .poId(shipment.getPoId())
                .subPoId(shipment.getSubPoId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originNodeId(shipment.getOriginNodeId())
                .destinationNodeId(shipment.getDestinationNodeId())
                .currentNodeId(shipment.getCurrentNodeId())
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .build();
    }

    public ShipmentResponseDto trackShipment(String publicId, TrackShipmentRequestDto dto){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        validateTrackRequest(dto);

        ShipmentCheckpoint checkpoint = ShipmentCheckpoint.builder()
                .shipmentId(shipment.getId())
                .nodeId(dto.getNodeId())
                .checkpointType(dto.getCheckpointType())
                .checkpointStatus(dto.getCheckpointStatus())
                .plannedAt(dto.getPlannedAt())
                .actualAt(dto.getActualAt())
                .note(dto.getNote())
                .build();

        shipmentCheckpointRepository.save(checkpoint);

        applyCheckpointToShipment(shipment, dto);

        Shipment updateShipment = shipmentRepository.save(shipment);
        return toResponseDto(updateShipment);
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
}
