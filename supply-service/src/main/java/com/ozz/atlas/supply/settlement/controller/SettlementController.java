package com.ozz.atlas.supply.settlement.controller;

import com.ozz.atlas.supply.settlement.dtos.CreateSettlementRequestDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supply/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

//    정산 생성
    @PostMapping
    public ResponseEntity<SettlementResponseDto> createSettlement(
            @Valid @RequestBody CreateSettlementRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(request));
    }

//    정산 목록 조회
    @GetMapping
    public ResponseEntity<List<SettlementResponseDto>> getSettlements() {
        return ResponseEntity.ok(settlementService.getSettlements());
    }

//    정산 상세 조회
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(
            @PathVariable Long settlementId
    ) {
        return ResponseEntity.ok(settlementService.getSettlement(settlementId));
    }

//    정산 승인
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
    @PatchMapping("/{settlementId}/cancel")
    public ResponseEntity<SettlementResponseDto> cancelSettlement(
            @PathVariable Long settlementId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String cancelledByUserPublicId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(settlementService.cancelSettlement(settlementId, cancelledByUserPublicId, userRole));
    }
}
