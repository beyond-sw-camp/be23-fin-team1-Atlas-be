package com.ozz.atlas.supply.shipment.lotmapping.controller;

import com.ozz.atlas.supply.shipment.lotmapping.dtos.CreateShipmentLotMappingRequestDto;
import com.ozz.atlas.supply.shipment.lotmapping.dtos.ShipmentLotMappingResponseDto;
import com.ozz.atlas.supply.shipment.lotmapping.service.ShipmentLotMappingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supply/shipments/{shipmentPublicId}/lots")
public class ShipmentLotMappingController {

    private final ShipmentLotMappingService shipmentLotMappingService;

    public ShipmentLotMappingController(ShipmentLotMappingService shipmentLotMappingService) {
        this.shipmentLotMappingService = shipmentLotMappingService;
    }

    @PostMapping
    public ResponseEntity<ShipmentLotMappingResponseDto> createShipmentLotMapping(
            @PathVariable String shipmentPublicId,
            @Valid @RequestBody CreateShipmentLotMappingRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipmentLotMappingService.createShipmentLotMapping(shipmentPublicId, request));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentLotMappingResponseDto>> getShipmentLotMappings(
            @PathVariable String shipmentPublicId
    ) {
        return ResponseEntity.ok(
                shipmentLotMappingService.getShipmentLotMappings(shipmentPublicId)
        );
    }
}
