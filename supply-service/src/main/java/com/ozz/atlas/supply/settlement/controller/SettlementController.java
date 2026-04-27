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
            @Valid @RequestBody CreateSettlementRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(request));
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
            return settlementSearchService.search(pageable, searchDto);
        }

        return settlementService.getSettlements(pageable);
    }


    //    정산 상세 조회
    @Operation(summary = "정산 상세 조회")
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(
            @PathVariable Long settlementId
    ) {
        return ResponseEntity.ok(settlementService.getSettlement(settlementId));
    }

//    정산 승인
    @Operation(summary = "정산 승인")
    @PatchMapping("/{settlementId}/approve")
    public ResponseEntity<SettlementResponseDto> approveSettlement(
            @PathVariable Long settlementId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String approvedByUserPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                settlementService.approveSettlement(settlementId, approvedByUserPublicId, userRole)
        );
    }

//    정산 취소
    @Operation(summary = "정산 취소")
    @PatchMapping("/{settlementId}/cancel")
    public ResponseEntity<SettlementResponseDto> cancelSettlement(
            @PathVariable Long settlementId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String cancelledByUserPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(settlementService.cancelSettlement(settlementId, cancelledByUserPublicId, userRole));
    }
}
