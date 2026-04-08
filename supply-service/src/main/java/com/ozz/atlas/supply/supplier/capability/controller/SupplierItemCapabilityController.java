package com.ozz.atlas.supply.supplier.capability.controller;

import com.ozz.atlas.supply.supplier.capability.dtos.CreateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.dtos.UpdateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.service.SupplierItemCapabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers/{supplierPublicId}/item-capabilities")
public class SupplierItemCapabilityController {

    private final SupplierItemCapabilityService supplierItemCapabilityService;

    @PostMapping
    public ResponseEntity<?> createCapability(
            @PathVariable String supplierPublicId,
            @Valid @RequestBody CreateSupplierItemCapabilityRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierItemCapabilityService.createCapability(supplierPublicId, request));
    }

    @GetMapping
    public ResponseEntity<?> getCapabilities(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplierItemCapabilityService.getCapabilities(supplierPublicId));
    }

    @GetMapping("/{itemPublicId}")
    public ResponseEntity<?> getCapability(
            @PathVariable String supplierPublicId,
            @PathVariable String itemPublicId
    ) {
        return ResponseEntity.ok(supplierItemCapabilityService.getCapability(supplierPublicId, itemPublicId));
    }

    @PatchMapping("/{itemPublicId}")
    public ResponseEntity<?> updateCapability(
            @PathVariable String supplierPublicId,
            @PathVariable String itemPublicId,
            @Valid @RequestBody UpdateSupplierItemCapabilityRequest request
    ) {
        return ResponseEntity.ok(
                supplierItemCapabilityService.updateCapability(supplierPublicId, itemPublicId, request)
        );
    }
}
