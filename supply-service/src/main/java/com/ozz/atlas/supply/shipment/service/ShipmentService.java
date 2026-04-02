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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
