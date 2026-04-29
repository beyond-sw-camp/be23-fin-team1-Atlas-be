package com.ozz.atlas.supply.batch.controller;

import com.ozz.atlas.supply.batch.dtos.BatchJobRunResponse;
import com.ozz.atlas.supply.batch.service.SupplyBatchJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply/batch")
@RequiredArgsConstructor
@Tag(name = "SupplyBatch")
public class SupplyBatchController {

    private final SupplyBatchJobService supplyBatchJobService;

    @Operation(summary = "인증서 만료 경고 배치 실행")
    @PostMapping("/jobs/certificate-expiry-warning")
    public ResponseEntity<BatchJobRunResponse> runCertificateExpiryWarning(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate runDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runCertificateExpiryWarning(runDate));
    }

    @Operation(summary = "협력사 배송 KPI 배치 실행")
    @PostMapping("/jobs/supplier-delivery-kpi")
    public ResponseEntity<BatchJobRunResponse> runSupplierDeliveryKpi(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate runDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runSupplierDeliveryKpi(runDate));
    }

    @Operation(summary = "일일 공급망 배치 실행")
    @PostMapping("/jobs/daily")
    public ResponseEntity<List<BatchJobRunResponse>> runDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate baseDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runDaily(baseDate));
    }
}
