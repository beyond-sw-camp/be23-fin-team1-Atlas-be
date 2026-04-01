package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.dtos.TrackShipmentRequestDto;
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

//    출하 목록 조회
    @GetMapping
    public ResponseEntity<List<ShipmentListResponseDto>> getShipments(){
        return ResponseEntity.ok(shipmentService.getShipments());
    }

//    출하 생성
    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody CreateShipmentRequestDto dto){
        ShipmentResponseDto createdShipment = shipmentService.createShipment(dto);

        return ResponseEntity
                .created(URI.create("/shipments/" + createdShipment.getPublicId()))
                .body(createdShipment);
    }

//    출하 상세 조회
    @GetMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> getShipment(@PathVariable String publicId) {
        return ResponseEntity.ok(shipmentService.getShipmentByPublicId(publicId));
    }

    @PostMapping("/{publicId}/track")
    public ResponseEntity<ShipmentResponseDto> trackShipment(
            @PathVariable String publicId,
            @Valid @RequestBody TrackShipmentRequestDto dto
    ) {
        return ResponseEntity.ok(shipmentService.trackShipment(publicId, dto));
    }

}
