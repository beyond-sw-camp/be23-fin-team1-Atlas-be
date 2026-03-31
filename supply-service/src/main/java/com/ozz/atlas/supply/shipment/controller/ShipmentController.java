package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponseDto>> getShipments(){
        return ResponseEntity.ok(shipmentService.getShipments());
    }

    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody CreateShipmentRequestDto dto){
        ShipmentResponseDto createdShipment = shipmentService.createShipment(dto);

        return ResponseEntity
                .created(URI.create("/shipments/" + createdShipment.getPublicId()))
                .body(createdShipment);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> getShipment(@PathVariable String publicId) {
        return ResponseEntity.ok(shipmentService.getShipmentByPublicId(publicId));
    }

}
