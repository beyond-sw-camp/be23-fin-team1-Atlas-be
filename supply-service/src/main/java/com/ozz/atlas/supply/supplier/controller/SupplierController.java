package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.dtos.CreateSupplierRequest;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<?> createSupplier(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(userRole, request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySupplier(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                supplierService.getMySupplier(organizationPublicId, organizationType)
        );
    }

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

    @DeleteMapping("/{supplierPublicId}/status")
    public ResponseEntity<?> deleteSupplier(
            @PathVariable String supplierPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId
    ) {
        supplierService.deleteSupplier(supplierPublicId, organizationPublicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/connections/summary")
    public ResponseEntity<?> getConnectedSupplierSummary(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                supplierService.getConnectedSupplierSummary(organizationPublicId, organizationType)
        );
    }

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



}
