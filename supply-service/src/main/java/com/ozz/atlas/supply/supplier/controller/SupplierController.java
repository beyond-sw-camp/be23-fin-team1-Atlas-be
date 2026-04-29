package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.dtos.CreateSupplierRequest;
import com.ozz.atlas.supply.supplier.dtos.OrganizationSupplySummaryDto;
import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "협력사 생성")
    @PostMapping
    public ResponseEntity<?> createSupplier(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(userRole, request));
    }

    @Operation(summary = "내 협력사 조회")
    @GetMapping("/me")
    public ResponseEntity<?> getMySupplier(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                supplierService.getMySupplier(organizationPublicId, organizationType)
        );
    }

    @Operation(summary = "협력사 상세 조회")
    @GetMapping("/{supplierPublicId}")
    public ResponseEntity<?> getSupplier(
            @PathVariable String supplierPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        return ResponseEntity.ok(
                supplierService.getSupplier(
                        supplierPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }


    @Operation(summary = "협력사 목록 조회")
    @GetMapping
    public ResponseEntity<?> getSupplerList(
            SupplierSearchDto supplierSearchDto,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                supplierService.getSupplierList(
                        pageable,
                        supplierSearchDto,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @Operation(summary = "협력사 수정")
    @PutMapping("/{supplierPublicId}")
    public ResponseEntity<?> updateSupplier(
            @PathVariable String supplierPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return ResponseEntity.ok(
                supplierService.updateSupplier(supplierPublicId, organizationPublicId, request)
        );
    }

    @Operation(summary = "협력사 상태 삭제 처리")
    @DeleteMapping("/{supplierPublicId}/status")
    public ResponseEntity<?> deleteSupplier(
            @PathVariable String supplierPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId
    ) {
        supplierService.deleteSupplier(supplierPublicId, organizationPublicId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "연결 협력사 요약 조회")
    @GetMapping("/connections/summary")
    public ResponseEntity<?> getConnectedSupplierSummary(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                supplierService.getConnectedSupplierSummary(organizationPublicId, organizationType)
        );
    }

    @Operation(summary = "연결 협력사 상세 조회")
    @GetMapping("/{supplierPublicId}/connections/detail")
    public ResponseEntity<?> getConnectedSupplierDetail(
            @PathVariable String supplierPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                supplierService.getConnectedSupplierDetail(
                        supplierPublicId,
                        organizationPublicId,
                        organizationType
                )
        );
    }

    @GetMapping("/organizations/{organizationPublicId}/summary")
    public ResponseEntity<OrganizationSupplySummaryDto> getOrganizationSupplySummary(
            @PathVariable String organizationPublicId
    ) {
        return ResponseEntity.ok(supplierService.getOrganizationSupplySummary(organizationPublicId));
    }




}
