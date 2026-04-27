package com.ozz.atlas.supply.returns.controller;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import com.ozz.atlas.supply.returns.dtos.CreateReturnRequestDto;
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
                                              "returnReason": "유통기한 임박 품목 회수",
                                              "attachmentPublicIds": ["att_01HZY2ATT10"],
                                              "items": [
                                                {
                                                  "itemPublicId": "item_01HZY2ITEM123456789",
                                                  "lotPublicId": "lot_01HZY2LOT123456789",
                                                  "returnQty": 120.5,
                                                  "unit": "BOX",
                                                  "detailReason": "포장 파손 및 냉장 온도 이탈",
                                                  "attachmentPublicIds": ["att_01HZY2ATT01"]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "반품 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ReturnRequestResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 7,
                                              "publicId": "ret_01HZY2RET123456789",
                                              "returnNumber": "RET-2026-0007",
                                              "sourceShipmentPublicId": "ship_01HZY1SHIPMENT123456789",
                                              "requestOrganizationPublicId": "org_req_01HZY2ORGREQ123456",
                                              "targetOrganizationPublicId": "org_tgt_01HZY2ORGTGT123456",
                                              "returnType": "DAMAGE",
                                              "returnReason": "유통기한 임박 품목 회수",
                                              "returnStatus": "REQUESTED",
                                              "requestedAt": "2026-04-17T10:10:00",
                                              "approvedAt": null,
                                              "completedAt": null,
                                              "createdByUserPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "attachmentPublicIds": ["att_01HZY2ATT10"],
                                              "items": [
                                                {
                                                  "id": 1,
                                                  "itemPublicId": "item_01HZY2ITEM123456789",
                                                  "lotPublicId": "lot_01HZY2LOT123456789",
                                                  "returnQty": 120.5,
                                                  "unit": "BOX",
                                                  "detailReason": "포장 파손 및 냉장 온도 이탈",
                                                  "itemStatus": "PENDING",
                                                  "attachmentPublicIds": ["att_01HZY2ATT01"]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ReturnRequestResponseDto> createReturn(
            @Valid @RequestBody CreateReturnRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                returnService.createReturn(
                        request,
                        actorPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllReturns(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "requestOrganizationPublicId", required = false) String requestOrganizationPublicId,
            @RequestParam(value = "targetOrganizationPublicId", required = false) String targetOrganizationPublicId,
            @RequestParam(value = "sourceShipmentPublicId", required = false) String sourceShipmentPublicId,
            @RequestParam(value = "returnType", required = false) ReturnType returnType,
            @RequestParam(value = "returnStatus", required = false) ReturnStatus returnStatus,
            @RequestParam(value = "itemPublicId", required = false) String itemPublicId,
            @RequestParam(value = "lotPublicId", required = false) String lotPublicId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // 컨트롤러에서 받은 검색 조건을 ES 검색 DTO로 묶습니다.
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .keyword(keyword)
                .requestOrganizationPublicId(requestOrganizationPublicId)
                .targetOrganizationPublicId(targetOrganizationPublicId)
                .sourceShipmentPublicId(sourceShipmentPublicId)
                .returnType(returnType)
                .returnStatus(returnStatus)
                .itemPublicId(itemPublicId)
                .lotPublicId(lotPublicId)
                .build();

        // 검색 조건이 하나라도 있으면 ES 검색으로 보냄
        if (returnSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(
                    returnSearchService.search(
                            pageable,
                            searchDto,
                            organizationPublicId,
                            organizationType,
                            userRole
                    )
            );
        }

        // 검색 조건이 없으면 기존 DB 페이지 목록을 사용
        return ResponseEntity.ok(
                returnService.getAllReturns(
                        pageable,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ReturnRequestResponseDto> getReturn(
            @PathVariable String publicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                returnService.getReturnByPublicId(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @GetMapping("/{publicId}/histories")
    public ResponseEntity<List<ReturnStatusHistoryResponseDto>> getReturnHistories(
            @PathVariable String publicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                returnService.getReturnHistories(
                        publicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ReturnRequestResponseDto> updateReturn(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                returnService.updateReturn(
                        publicId,
                        request,
                        actorPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @PatchMapping("/{publicId}/status")
    public ResponseEntity<ReturnRequestResponseDto> changeStatus(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnStatusDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                returnService.changeStatus(
                        publicId,
                        request,
                        actorPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }
}