package com.ozz.atlas.supply.batch.controller;

import com.ozz.atlas.supply.batch.dtos.BatchJobRunResponse;
import com.ozz.atlas.supply.batch.service.SupplyBatchJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/supply/batch")
@RequiredArgsConstructor
public class SupplyBatchController {

    private final SupplyBatchJobService supplyBatchJobService;

    @PostMapping("/jobs/certificate-expiry-warning")
    public ResponseEntity<BatchJobRunResponse> runCertificateExpiryWarning(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate runDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runCertificateExpiryWarning(runDate));
    }

    @PostMapping("/jobs/lot-expiry-aggregation")
    public ResponseEntity<BatchJobRunResponse> runLotExpiryAggregation(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate runDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runLotExpiryAggregation(runDate));
    }

    @PostMapping("/jobs/supplier-delivery-kpi")
    public ResponseEntity<BatchJobRunResponse> runSupplierDeliveryKpi(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate runDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runSupplierDeliveryKpi(runDate));
    }

    @PostMapping("/jobs/daily")
    public ResponseEntity<List<BatchJobRunResponse>> runDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate baseDate
    ) {
        return ResponseEntity.ok(supplyBatchJobService.runDaily(baseDate));
    }
}
