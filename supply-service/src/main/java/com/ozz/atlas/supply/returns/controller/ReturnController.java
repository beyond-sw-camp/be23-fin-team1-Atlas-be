package com.ozz.atlas.supply.returns.controller;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import com.ozz.atlas.supply.returns.dtos.CreateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.InspectReturnItemRequestDto;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.dtos.ReturnStatusHistoryResponseDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnStatusDto;
import com.ozz.atlas.supply.returns.search.dtos.ReturnSearchDto;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import com.ozz.atlas.supply.returns.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/returns")
@Tag(name = "Return")
public class ReturnController {

    private final ReturnService returnService;
    private final ReturnSearchService returnSearchService;

    @Operation(summary = "반품 품목 검수 결과 입력", description = "품목별 QC 상태와 등급을 입력한다.")
    @PatchMapping("/{publicId}/items/{itemId}/inspect")
    public ResponseEntity<ReturnRequestResponseDto> inspectItem(
            @PathVariable String publicId,
            @PathVariable Long itemId,
            @Valid @RequestBody InspectReturnItemRequestDto request,
            @RequestHeader("X-Public-Id") String actorPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId
    ) {
        return ResponseEntity.ok(returnService.inspectItem(publicId, itemId, request, actorPublicId, organizationPublicId));
    }

    @PostMapping
    @Operation(
            summary = "반품 생성",
            description = "반품 요청과 반품 품목 목록을 함께 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateReturnRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "sourceShipmentPublicId": "ship_01HZY1SHIPMENT123456789",
                                              "returnType": "DAMAGE",
                                              "resolutionType": "RETURN",
                                              "returnReason": "박스 파손",
                                              "items": [
                                                {
                                                  "itemPublicId": "itm_01HZY1ITEM123456789",
                                                  "returnQty": 2,
                                                  "unit": "BOX",
                                                  "detailReason": "모서리 찌그러짐"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "반품 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    public ResponseEntity<ReturnRequestResponseDto> createReturn(
            @Valid @RequestBody CreateReturnRequestDto request,
            @RequestHeader("X-Public-Id") String actorPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(returnService.createReturn(request, actorPublicId, organizationPublicId, organizationType, userRole));
    }

    @GetMapping
    @Operation(summary = "반품 목록 조회", description = "조직 식별자를 기반으로 반품 목록을 조회한다.")
    public ResponseEntity<org.springframework.data.domain.Page<ReturnRequestResponseDto>> getAllReturns(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(returnService.getAllReturns(pageable, organizationPublicId, organizationType, userRole));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "반품 상세 조회")
    public ResponseEntity<ReturnRequestResponseDto> getReturn(
            @PathVariable String publicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(returnService.getReturnByPublicId(publicId, organizationPublicId, organizationType, userRole));
    }

    @GetMapping("/{publicId}/exists")
    @Operation(summary = "반품 존재 여부 확인", description = "시스템 및 제3자 연동용 단순 존재 여부 검사")
    public ResponseEntity<Boolean> existsReturn(@PathVariable String publicId) {
        return ResponseEntity.ok(returnService.existsByPublicId(publicId));
    }

    @PatchMapping("/{publicId}")
    @Operation(summary = "반품 수정")
    public ResponseEntity<ReturnRequestResponseDto> updateReturn(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnRequestDto request,
            @RequestHeader("X-Public-Id") String actorPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(returnService.updateReturn(publicId, request, actorPublicId, organizationPublicId, organizationType, userRole));
    }

    @PatchMapping("/{publicId}/status")
    @Operation(summary = "반품 상태 변경", description = "반품의 상태를 변경하고 필요한 경우 후속 조치(출하 생성 등)를 수행한다.")
    public ResponseEntity<ReturnRequestResponseDto> changeStatus(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnStatusDto request,
            @RequestHeader("X-Public-Id") String actorPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(returnService.changeStatus(publicId, request, actorPublicId, organizationPublicId, organizationType, userRole));
    }

    @GetMapping("/{publicId}/histories")
    @Operation(summary = "반품 상태 이력 조회")
    public ResponseEntity<List<ReturnStatusHistoryResponseDto>> getReturnHistories(
            @PathVariable String publicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(returnService.getReturnHistories(publicId, organizationPublicId, organizationType, userRole));
    }

    @GetMapping("/search")
    @Operation(summary = "반품 통합 검색 (ES)")
    public ResponseEntity<org.springframework.data.domain.Page<ReturnRequestResponseDto>> searchReturns(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(required = false) ReturnType type,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Role") String userRole,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .keyword(keyword)
                .returnStatus(status)
                .returnType(type)
                .build();
        return ResponseEntity.ok(returnSearchService.search(pageable, searchDto, organizationPublicId, organizationType, userRole));
    }
}
