package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/{supplierPublicId}")
    public ResponseEntity<?> getSupplier(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierPublicId));
    }

    @GetMapping
    public ResponseEntity<?> getSupplerList(
            SupplierSearchDto supplierSearchDto,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supplierService.getSupplierList(pageable, supplierSearchDto));
    }

    @GetMapping("/tier/{tierLevel}")
    public ResponseEntity<?> getSuppliersByTierLevel(
            @PathVariable Integer tierLevel,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(supplierService.getSuppliersByTierLevel(tierLevel, pageable));
    }

    @PutMapping("/{supplierPublicId}")
    public ResponseEntity<?> updateSupplier(@PathVariable String supplierPublicId, @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierPublicId, request));
    }

    @DeleteMapping("/{supplierPublicId}/status")
    public ResponseEntity<?> deleteSupplier(@PathVariable String supplierPublicId) {
        supplierService.deleteSupplier(supplierPublicId);
        return ResponseEntity.noContent().build();
    }

}
