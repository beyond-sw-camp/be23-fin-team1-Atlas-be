package com.ozz.atlas.supply.shipment.lotmapping.controller;

import com.ozz.atlas.supply.shipment.lotmapping.dtos.CreateShipmentLotMappingRequestDto;
import com.ozz.atlas.supply.shipment.lotmapping.dtos.ShipmentLotMappingResponseDto;
import com.ozz.atlas.supply.shipment.lotmapping.service.ShipmentLotMappingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply/shipments/{shipmentPublicId}/lots")
@Tag(name = "ShipmentLotMapping")
public class ShipmentLotMappingController {

    private final ShipmentLotMappingService shipmentLotMappingService;

    public ShipmentLotMappingController(ShipmentLotMappingService shipmentLotMappingService) {
        this.shipmentLotMappingService = shipmentLotMappingService;
    }

    @Operation(summary = "출하 LOT 매핑 생성")
    @PostMapping
    public ResponseEntity<ShipmentLotMappingResponseDto> createShipmentLotMapping(
            @PathVariable String shipmentPublicId,
            @Valid @RequestBody CreateShipmentLotMappingRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipmentLotMappingService.createShipmentLotMapping(shipmentPublicId, request));
    }

    @Operation(summary = "출하 LOT 매핑 목록 조회")
    @GetMapping
    public ResponseEntity<List<ShipmentLotMappingResponseDto>> getShipmentLotMappings(
            @PathVariable String shipmentPublicId
    ) {
        return ResponseEntity.ok(
                shipmentLotMappingService.getShipmentLotMappings(shipmentPublicId)
        );
    }
}
