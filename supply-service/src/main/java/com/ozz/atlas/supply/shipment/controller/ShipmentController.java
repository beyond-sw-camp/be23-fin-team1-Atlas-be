package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.*;
import com.ozz.atlas.supply.shipment.search.dtos.ShipmentSearchDto;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import com.ozz.atlas.supply.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/supply/shipments")
@Tag(name = "Shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentSearchService shipmentSearchService;

    public ShipmentController(ShipmentService shipmentService, ShipmentSearchService shipmentSearchService) {
        this.shipmentService = shipmentService;
        this.shipmentSearchService = shipmentSearchService;
    }

//    출하 생성
    @PostMapping
    @Operation(
            summary = "출하 생성",
            description = "발주 기준으로 출하 정보를 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateShipmentRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "shipmentNumber": "SHIP-2026-0001",
                                              "poId": 101,
                                              "purchaseOrderPublicId": "po_01HZY1PO123456789",
                                              "subPoId": 202,
                                              "subPurchaseOrderPublicId": "subpo_01HZY1SUBPO123456789",
                                              "carrierName": "CJ Logistics",
                                              "vehicleNo": "12가3456",
                                              "trackingNo": "TRK-ATLAS-20260417",
                                              "originNodePublicId": "node_origin_01HZY1AAA",
                                              "destinationNodePublicId": "node_dest_01HZY1BBB",
                                              "departureEta": "2026-04-18T08:00:00",
                                              "arrivalEta": "2026-04-18T14:00:00",
                                              "temperatureRequired": true
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "출하 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ShipmentResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "publicId": "ship_01HZY1SHIPMENT123456789",
                                              "shipmentNumber": "SHIP-2026-0001",
                                              "poId": 101,
                                              "purchaseOrderPublicId": "po_01HZY1PO123456789",
                                              "subPoId": 202,
                                              "subPurchaseOrderPublicId": "subpo_01HZY1SUBPO123456789",
                                              "carrierName": "CJ Logistics",
                                              "vehicleNo": "12가3456",
                                              "trackingNo": "TRK-ATLAS-20260417",
                                              "originNodePublicId": "node_origin_01HZY1AAA",
                                              "destinationNodePublicId": "node_dest_01HZY1BBB",
                                              "currentNodePublicId": "node_origin_01HZY1AAA",
                                              "departureEta": "2026-04-18T08:00:00",
                                              "arrivalEta": "2026-04-18T14:00:00",
                                              "actualDepartedAt": null,
                                              "actualArrivedAt": null,
                                              "status": "READY",
                                              "temperatureRequired": true
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody CreateShipmentRequestDto dto){
        ShipmentResponseDto createdShipment = shipmentService.createShipment(dto);

        return ResponseEntity
                .created(URI.create("/api/supply/shipments/" + createdShipment.getPublicId()))
                .body(createdShipment);
    }

//    출하 목록 조회
// 검색 조건이 있으면 ES 검색을 사용하고, 없으면 기존 JPA 목록 조회를 유지
    @GetMapping
    public Page<ShipmentListResponseDto> getShipments(@ModelAttribute ShipmentSearchDto searchDto,
                                                      @PageableDefault(size = 10, sort = "id",
                                                      direction = Sort.Direction.ASC) Pageable pageable){
        if (shipmentSearchService.hasSearchCondition(searchDto)){
            return shipmentSearchService.search(pageable,searchDto);
        }
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

//    ETA projection 조회
    @GetMapping("/{publicId}/eta-projections")
    public ResponseEntity<List<EtaProjectionResponseDto>> getEtaProjections(@PathVariable String publicId) {
        return ResponseEntity.ok(shipmentService.getEtaProjections(publicId));
    }

    //    statusHistory 조회
    @GetMapping("/{publicId}/status-history")
    public ResponseEntity<List<ShipmentStatusHistoryResponseDto>> getShipmentStatusHistories(@PathVariable String publicId){
        return ResponseEntity.ok(shipmentService.getShipmentStatusHistories(publicId));
    }
}
