package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public List<ShipmentResponseDto> getShipments(){
        return shipmentRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ShipmentResponseDto getShipmentByPublicId(String publicId){
        Shipment shipment = shipmentRepository.findByPublicId(publicId)
                .orElseThrow(()->new IllegalArgumentException("출하 정보를 찾을 수 없습니다."));

        return toResponseDto(shipment);
    }
    @Transactional
    public ShipmentResponseDto createShipment(CreateShipmentRequestDto dto){
        Shipment shipment = Shipment.builder()
                .publicId(generatePublicId())
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

    private String generatePublicId(){
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 26);
    }
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

}
