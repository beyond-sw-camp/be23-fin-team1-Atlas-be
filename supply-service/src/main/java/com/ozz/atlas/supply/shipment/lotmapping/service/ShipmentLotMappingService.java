package com.ozz.atlas.supply.shipment.lotmapping.service;

import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.repository.LotRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.lotmapping.domain.ShipmentLotMapping;
import com.ozz.atlas.supply.shipment.lotmapping.dtos.CreateShipmentLotMappingRequestDto;
import com.ozz.atlas.supply.shipment.lotmapping.dtos.ShipmentLotMappingResponseDto;
import com.ozz.atlas.supply.shipment.lotmapping.repository.ShipmentLotMappingRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ShipmentLotMappingService {

    private final ShipmentRepository shipmentRepository;
    private final LotRepository lotRepository;
    private final ShipmentLotMappingRepository shipmentLotMappingRepository;

    public ShipmentLotMappingService(
            ShipmentRepository shipmentRepository,
            LotRepository lotRepository,
            ShipmentLotMappingRepository shipmentLotMappingRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.lotRepository = lotRepository;
        this.shipmentLotMappingRepository = shipmentLotMappingRepository;
    }

    public ShipmentLotMappingResponseDto createShipmentLotMapping(
            String shipmentPublicId,
            CreateShipmentLotMappingRequestDto request
    ) {
        Shipment shipment = shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        Lot lot = lotRepository.findByPublicId(request.getLotPublicId())
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.LOT_NOT_FOUND));

        if (request.getShippedQty() == null || request.getShippedQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_LOT_MAPPING_REQUEST);
        }

        if (shipmentLotMappingRepository.existsByShipment_IdAndLot_Id(shipment.getId(), lot.getId())) {
            throw new ShipmentException(ShipmentErrorCode.DUPLICATE_SHIPMENT_LOT_MAPPING);
        }

        BigDecimal totalShippedQty = shipmentLotMappingRepository.sumShippedQtyByLotId(lot.getId());
        BigDecimal availableQty = lot.getQty().subtract(totalShippedQty);

        if (request.getShippedQty().compareTo(availableQty) > 0) {
            throw new ShipmentException(ShipmentErrorCode.INVALID_SHIPMENT_LOT_MAPPING_REQUEST);
        }

        ShipmentLotMapping shipmentLotMapping = ShipmentLotMapping.builder()
                .shipment(shipment)
                .lot(lot)
                .shippedQty(request.getShippedQty())
                .unit(lot.getUnit())
                .loadedAt(java.time.LocalDateTime.now())
                .build();

        ShipmentLotMapping saved = shipmentLotMappingRepository.save(shipmentLotMapping);

        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ShipmentLotMappingResponseDto> getShipmentLotMappings(String shipmentPublicId) {
        Shipment shipment = shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return shipmentLotMappingRepository.findAllByShipment_IdOrderByIdDesc(shipment.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    private ShipmentLotMappingResponseDto toResponseDto(ShipmentLotMapping entity) {
        return ShipmentLotMappingResponseDto.builder()
                .shipmentPublicId(entity.getShipment().getPublicId())
                .lotPublicId(entity.getLot().getPublicId())
                .shippedQty(entity.getShippedQty())
                .unit(entity.getUnit())
                .loadedAt(entity.getLoadedAt())
                .build();
    }
}
