package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.shipment.domain.DeliveryException;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionType;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.dtos.CreateDeliveryExceptionRequestDto;
import com.ozz.atlas.supply.shipment.dtos.DeliveryExceptionResponseDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;
import com.ozz.atlas.supply.shipment.repository.DeliveryExceptionRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryExceptionService {

    private final DeliveryExceptionRepository deliveryExceptionRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentSearchService shipmentSearchService;

    public DeliveryExceptionService(
            DeliveryExceptionRepository deliveryExceptionRepository,
            ShipmentRepository shipmentRepository, ShipmentSearchService shipmentSearchService
    ) {
        this.deliveryExceptionRepository = deliveryExceptionRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentSearchService = shipmentSearchService;
    }

    public DeliveryExceptionResponseDto createDeliveryException(CreateDeliveryExceptionRequestDto dto) {
        Shipment shipment = shipmentRepository.findByPublicId(dto.getShipmentPublicId())
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        DeliveryException deliveryException = DeliveryException.builder()
                .shipmentId(shipment.getId())
                .shipmentCheckpointId(null)
                .exceptionType(dto.getExceptionType())
                .severity(dto.getSeverity())
                .detectedAt(dto.getDetectedAt())
                .note(dto.getNote())
                .resolved(false)
                .build();

        DeliveryException savedException = deliveryExceptionRepository.save(deliveryException);

        if (dto.getExceptionType() == DeliveryExceptionType.DELAY) {
            shipment.markDelayed();
        }
        shipmentSearchService.saveShipmentDocument(shipment);


        return DeliveryExceptionResponseDto.from(savedException, shipment.getPublicId());
    }

    @Transactional(readOnly = true)
    public List<DeliveryExceptionResponseDto> getDeliveryExceptionsByShipmentPublicId(String shipmentPublicId) {
        Shipment shipment = shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new ShipmentException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return deliveryExceptionRepository.findByShipmentIdOrderByDetectedAtDesc(shipment.getId())
                .stream()
                .map(deliveryException -> DeliveryExceptionResponseDto.from(deliveryException, shipment.getPublicId()))
                .toList();
    }
}
