package com.ozz.atlas.supply.settlement.controller;

import com.ozz.atlas.supply.settlement.dtos.CreateSettlementRequestDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import com.ozz.atlas.supply.settlement.search.dtos.SettlementSearchDto;
import com.ozz.atlas.supply.settlement.search.service.SettlementSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ozz.atlas.supply.settlement.dtos.SettlementStatisticsResponseDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementBudgetRequestDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementBudgetResponseDto;




@RestController
@RequestMapping("/api/supply/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementSearchService settlementSearchService;

//    정산 생성
    @Operation(summary = "정산 생성")
    @PostMapping
    public ResponseEntity<SettlementResponseDto> createSettlement(
            @Valid @RequestBody CreateSettlementRequestDto request,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(request, organizationPublicId, userRole));
    }

    //    정산 목록 조회
    @Operation(summary = "정산 목록 조회")
    @GetMapping
    public Page<SettlementResponseDto> getSettlements(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
            @RequestParam(value = "targetType", required = false) SettlementTargetType targetType,
            @RequestParam(value = "settlementStatus", required = false) SettlementStatus settlementStatus,
            @RequestParam(value = "currencyCode", required = false) SettlementCurrency currencyCode,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SettlementSearchDto searchDto = SettlementSearchDto.builder()
                .keyword(keyword)
                .supplierPublicId(supplierPublicId)
                .targetType(targetType)
                .settlementStatus(settlementStatus)
                .currencyCode(currencyCode)
                .build();

        if (settlementSearchService.hasSearchCondition(searchDto)) {
            return settlementService.searchSettlements(pageable, searchDto, organizationPublicId, userRole);
        }

        return settlementService.getSettlements(pageable, organizationPublicId, userRole);
    }

    // 정산 통계 조회
    // 정산 대시보드 카드와 차트에 사용할 집계 데이터를 조회
    @Operation(summary = "정산 통계 조회")
    @GetMapping("/statistics")
    public ResponseEntity<SettlementStatisticsResponseDto> getSettlementStatistics(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.getSettlementStatistics(year, organizationPublicId, userRole)
        );
    }

    //    정산 상세 조회
    @Operation(summary = "정산 상세 조회")
    @GetMapping("/{settlementPublicId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(
            @PathVariable String settlementPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.getSettlement(settlementPublicId, organizationPublicId, userRole)
        );
    }

    //    정산 승인
    @Operation(summary = "정산 승인")
    @PatchMapping("/{settlementPublicId}/approve")
    public ResponseEntity<SettlementResponseDto> approveSettlement(
            @PathVariable String settlementPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String approvedByUserPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.approveSettlement(
                        settlementPublicId,
                        organizationPublicId,
                        approvedByUserPublicId,
                        userRole
                )
        );
    }

    //    정산 취소
    @Operation(summary = "정산 취소")
    @PatchMapping("/{settlementPublicId}/cancel")
    public ResponseEntity<SettlementResponseDto> cancelSettlement(
            @PathVariable String settlementPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String cancelledByUserPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.cancelSettlement(
                        settlementPublicId,
                        organizationPublicId,
                        cancelledByUserPublicId,
                        userRole
                )
        );
    }

    // 월별 정산 예산 저장/수정
    @Operation(summary = "정산 예산 저장/수정")
    @PutMapping("/budgets")
    public ResponseEntity<SettlementBudgetResponseDto> saveSettlementBudget(
            @Valid @RequestBody SettlementBudgetRequestDto request,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String userPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.saveSettlementBudget(
                        request,
                        organizationPublicId,
                        userPublicId,
                        userRole
                )
        );
    }

}
