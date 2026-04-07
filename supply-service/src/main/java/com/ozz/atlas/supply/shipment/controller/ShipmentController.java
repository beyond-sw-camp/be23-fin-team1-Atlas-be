package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.*;
import com.ozz.atlas.supply.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

//    출하 생성
    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody CreateShipmentRequestDto dto){
        ShipmentResponseDto createdShipment = shipmentService.createShipment(dto);

        return ResponseEntity
                .created(URI.create("/api/shipments/" + createdShipment.getPublicId()))
                .body(createdShipment);
    }

//    출하 목록 조회
    @GetMapping
    public Page<ShipmentListResponseDto> getShipments(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return shipmentService.getShipments(pageable);
    }

//    출하 상세 조회
    @GetMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> getShipment(@PathVariable String publicId) {
        return ResponseEntity.ok(shipmentService.getShipmentByPublicId(publicId));
    }

//    track 조회
    @PostMapping("/{publicId}/track")
    public ResponseEntity<ShipmentResponseDto> trackShipment(
            @PathVariable String publicId,
            @Valid @RequestBody TrackShipmentRequestDto dto
    ) {
        return ResponseEntity.ok(shipmentService.trackShipment(publicId, dto));
    }

//    ETA 조회
    @GetMapping("/{publicId}/eta")
    public ResponseEntity<ShipmentEtaResponseDto> getShipmentEta(@PathVariable String publicId){
        return ResponseEntity.ok(shipmentService.getShipmentEta(publicId));
    }

//    statusHistory 조회
    @GetMapping("/{publicId}/status-history")
    public ResponseEntity<List<ShipmentStatusHistoryResponseDto>> getShipmentStatusHistories(@PathVariable String publicId){
        return ResponseEntity.ok(shipmentService.getShipmentStatusHistories(publicId));
    }
}
