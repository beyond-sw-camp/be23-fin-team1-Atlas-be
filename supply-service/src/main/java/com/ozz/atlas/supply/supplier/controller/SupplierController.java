package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/{supplierId}")
    public ResponseEntity<?> getSupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @GetMapping
    public ResponseEntity<?> getSupplerList(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supplierService.getSupplierList(pageable));
    }

    @GetMapping("/tier/{tierLevel}")
    public ResponseEntity<?> getSuppliersByTierLevel(
            @PathVariable Integer tierLevel,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(supplierService.getSuppliersByTierLevel(tierLevel, pageable));
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<?> updateSupplier(@PathVariable Long supplierId, @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, request));
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }

}
