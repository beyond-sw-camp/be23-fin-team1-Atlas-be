package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateShipmentRequestDto;
import com.ozz.atlas.supply.shipment.dtos.EtaProjectionResponseDto;
import com.ozz.atlas.supply.shipment.dtos.ShipmentCreatableOrderDto;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply/shipments")
@Tag(name = "Shipment", description = "출하 생성, 조회, 수정, 상태 변경, 추적, 지도 데이터 API")
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
    @Operation(summary = "출하 생성", description = "승인된 발주와 출하 품목 라인을 기준으로 출하를 생성합니다. 생성 시 재고 차감과 출하 라인 저장이 함께 수행됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "출하 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류 또는 출하 가능 수량 부족"),
            @ApiResponse(responseCode = "403", description = "출하 생성 권한 없음")
    })
    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(
            @Parameter(description = "요청 사용자 공개 식별자", example = "01HQUSER789ABCDEF01HQUSER")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
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
    @Operation(summary = "출하 목록 조회", description = "조직 권한 범위에 맞는 출하 목록을 최신순으로 조회합니다. 검색 조건이 있으면 검색 인덱스를 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 조회 권한 없음")
    })
    @GetMapping
    public Page<ShipmentListResponseDto> getShipments(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
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

    @Operation(summary = "출하 검색 인덱스 재색인", description = "출하 검색 문서를 전체 재생성합니다. 관리자 권한에서만 사용할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "출하 검색 인덱스 재색인 성공"),
            @ApiResponse(responseCode = "403", description = "재색인 권한 없음")
    })
    @PostMapping("/reindex")
    public ResponseEntity<Void> reindexShipments(
            @Parameter(description = "요청 조직 유형", example = "ADMIN")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        if (!"ADMIN".equalsIgnoreCase(organizationType)
                && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }

        shipmentSearchService.reindexAllShipments();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "진행 중 출하 지도 데이터 조회", description = "지도 화면에 표시할 READY, IN_TRANSIT, DELAYED 상태의 출하 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 지도 데이터 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 지도 데이터 조회 권한 없음")
    })
    @GetMapping("/map")
    public ResponseEntity<List<ShipmentMapResponseDto>> getShipmentMapData(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
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

    @Operation(summary = "출하 생성 가능 발주 목록 조회", description = "로그인한 공급사가 출하 생성에 사용할 수 있는 승인 발주와 품목별 출하 가능 수량, 출발 창고 후보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 생성 가능 발주 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음")
    })
    @GetMapping("/creatable-orders")
    public ResponseEntity<List<ShipmentCreatableOrderDto>> getCreatableOrders(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                shipmentService.getCreatableOrders(
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // 출하 상세 조회
    @Operation(summary = "출하 상세 조회", description = "출하 공개 식별자로 상세 정보와 출하 품목 라인을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 상세 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @GetMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> getShipment(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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

    @Operation(summary = "출하 수정", description = "READY 상태 출하의 출발 예정 시각과 배송 옵션을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 수정 성공"),
            @ApiResponse(responseCode = "400", description = "수정 불가 상태 또는 요청값 오류"),
            @ApiResponse(responseCode = "403", description = "출하 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}")
    public ResponseEntity<ShipmentResponseDto> updateShipment(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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

    @Operation(summary = "배송중 처리", description = "READY 상태 출하를 IN_TRANSIT로 변경합니다. 운송사, 차량번호, 운송장번호와 도착 예정 시각은 자동 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송중 처리 성공"),
            @ApiResponse(responseCode = "400", description = "배송중 처리 불가 상태"),
            @ApiResponse(responseCode = "403", description = "배송중 처리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/start")
    public ResponseEntity<ShipmentResponseDto> startShipment(
            @Parameter(description = "요청 사용자 공개 식별자", example = "01HQUSER789ABCDEF01HQUSER")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.startShipment(
                        publicId,
                        actorUserPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @Operation(summary = "도착 완료 처리", description = "배송중 출하를 ARRIVED로 변경합니다. 물건을 받는 조직이 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "도착 완료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "도착 완료 처리 불가 상태"),
            @ApiResponse(responseCode = "403", description = "도착 완료 처리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/arrive")
    public ResponseEntity<ShipmentResponseDto> arriveShipment(
            @Parameter(description = "요청 사용자 공개 식별자", example = "01HQUSER789ABCDEF01HQUSER")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "BUYER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.arriveShipment(
                        publicId,
                        actorUserPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    // 출하 위치/상태 추적
    @Operation(summary = "출하 추적 정보 등록", description = "출하 체크포인트를 등록하고 현재 위치/상태 정보를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 추적 정보 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류 또는 추적 등록 불가 상태"),
            @ApiResponse(responseCode = "403", description = "추적 등록 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<ShipmentResponseDto> cancelShipment(
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                shipmentService.cancelShipment(
                        publicId,
                        actorUserPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @PostMapping("/{publicId}/track")
    @Operation(summary = "출하 추적 정보 등록", description = "출하 체크포인트와 현재 위치 정보를 등록하고 출하 추적 상태를 갱신한다.")
    public ResponseEntity<ShipmentResponseDto> trackShipment(
            @Parameter(description = "요청 사용자 공개 식별자", example = "01HQUSER789ABCDEF01HQUSER")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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
    @Operation(summary = "출하 ETA 조회", description = "출하의 현재 예상 도착 시각과 지연 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 ETA 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 ETA 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @GetMapping("/{publicId}/eta")
    public ResponseEntity<ShipmentEtaResponseDto> getShipmentEta(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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
    @Operation(summary = "출하 ETA 예측 목록 조회", description = "출하 ETA 계산 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 ETA 예측 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 ETA 예측 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @GetMapping("/{publicId}/eta-projections")
    public ResponseEntity<List<EtaProjectionResponseDto>> getEtaProjections(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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
    @Operation(summary = "출하 상태 이력 조회", description = "READY, IN_TRANSIT, ARRIVED 등 출하 상태 변경 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출하 상태 이력 조회 성공"),
            @ApiResponse(responseCode = "403", description = "출하 상태 이력 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @GetMapping("/{publicId}/status-history")
    public ResponseEntity<List<ShipmentStatusHistoryResponseDto>> getShipmentStatusHistories(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
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
