package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.EtaProjectionResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentEtaResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentStatusHistoryResponseDto;
import com.ozz.atlas.supply.shipment.dtos.TrackShipmentRequestDto;
import com.ozz.atlas.supply.shipment.search.dtos.ShipmentSearchDto;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import com.ozz.atlas.supply.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ozz.atlas.supply.shipment.dtos.UpdateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentMapResponseDto;
import com.ozz.atlas.supply.shipment.exception.ShipmentErrorCode;
import com.ozz.atlas.supply.shipment.exception.ShipmentException;

import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply/shipments")
@Tag(name = "Shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentSearchService shipmentSearchService;

    public ShipmentController(
            ShipmentService shipmentService,
            ShipmentSearchService shipmentSearchService
    ) {
        this.shipmentService = shipmentService;
        this.shipmentSearchService = shipmentSearchService;
    }

    // 출하 생성
    @Operation(summary = "출하 생성")
    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateShipmentRequestDto dto
    ) {
        ShipmentResponseDto createdShipment = shipmentService.createShipment(
                dto,
                actorUserPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );

        return ResponseEntity
                .created(URI.create("/api/supply/shipments/" + createdShipment.getPublicId()))
                .body(createdShipment);
    }

    // 출하 목록 조회
    @Operation(summary = "출하 목록 조회")
    @GetMapping
    public Page<ShipmentListResponseDto> getShipments(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @ModelAttribute ShipmentSearchDto searchDto,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (shipmentSearchService.hasSearchCondition(searchDto)) {
            return shipmentSearchService.search(
                    pageable,
                    searchDto,
                    organizationPublicId,
                    organizationType,
                    userRole
            );
        }

        return shipmentService.getShipments(
                organizationPublicId,
                organizationType,
                userRole,
                pageable
        );
    }
    @PostMapping("/reindex")
    public ResponseEntity<Void> reindexShipments(
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        if (!"ADMIN".equalsIgnoreCase(organizationType)
                && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        shipmentSearchService.reindexAllShipments();

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/map")
    public ResponseEntity<List<ShipmentMapResponseDto>> getShipmentMapData(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                shipmentService.getShipmentMapData(
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // 출하 상세 조회
    @Operation(summary = "출하 상세 조회")
    @GetMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> getShipment(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.getShipmentByPublicId(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }
    @PatchMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> updateShipment(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId,
            @RequestBody UpdateShipmentRequestDto dto
    ) {
        return ResponseEntity.ok(
                shipmentService.updateShipment(
                        publicId,
                        dto,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // 출하 위치/상태 추적
    @Operation(summary = "출하 추적 정보 등록")
    @PostMapping("/{publicId}/track")
    public ResponseEntity<ShipmentResponseDto> trackShipment(
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId,
            @Valid @RequestBody TrackShipmentRequestDto dto
    ) {
        return ResponseEntity.ok(
                shipmentService.trackShipment(
                        publicId,
                        dto,
                        actorUserPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // ETA 조회
    @Operation(summary = "출하 ETA 조회")
    @GetMapping("/{publicId}/eta")
    public ResponseEntity<ShipmentEtaResponseDto> getShipmentEta(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.getShipmentEta(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // ETA projection 조회
    @Operation(summary = "출하 ETA 예측 목록 조회")
    @GetMapping("/{publicId}/eta-projections")
    public ResponseEntity<List<EtaProjectionResponseDto>> getEtaProjections(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.getEtaProjections(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // 출하 상태 이력 조회
    @Operation(summary = "출하 상태 이력 조회")
    @GetMapping("/{publicId}/status-history")
    public ResponseEntity<List<ShipmentStatusHistoryResponseDto>> getShipmentStatusHistories(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.getShipmentStatusHistories(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }
}
