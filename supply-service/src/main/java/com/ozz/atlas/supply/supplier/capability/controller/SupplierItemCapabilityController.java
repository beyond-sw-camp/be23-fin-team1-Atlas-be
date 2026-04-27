package com.ozz.atlas.supply.supplier.capability.controller;

import com.ozz.atlas.supply.supplier.capability.dtos.CreateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.dtos.UpdateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.service.SupplierItemCapabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers/{supplierPublicId}/item-capabilities")
@Tag(name = "SupplierItemCapability")
public class SupplierItemCapabilityController {

    private final SupplierItemCapabilityService supplierItemCapabilityService;

    @Operation(summary = "협력사 품목 공급 역량 생성")
    @PostMapping
    public ResponseEntity<?> createCapability(
            @PathVariable String supplierPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody CreateSupplierItemCapabilityRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierItemCapabilityService.createCapability(supplierPublicId, request, actorUserPublicId));
    }

    @Operation(summary = "협력사 품목 공급 역량 목록 조회")
    @GetMapping
    public ResponseEntity<?> getCapabilities(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplierItemCapabilityService.getCapabilities(supplierPublicId));
    }

    @Operation(summary = "협력사 품목 공급 역량 상세 조회")
    @GetMapping("/{itemPublicId}")
    public ResponseEntity<?> getCapability(
            @PathVariable String supplierPublicId,
            @PathVariable String itemPublicId
    ) {
        return ResponseEntity.ok(supplierItemCapabilityService.getCapability(supplierPublicId, itemPublicId));
    }

    @Operation(summary = "협력사 품목 공급 역량 수정")
    @PatchMapping("/{itemPublicId}")
    public ResponseEntity<?> updateCapability(
            @PathVariable String supplierPublicId,
            @PathVariable String itemPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody UpdateSupplierItemCapabilityRequest request
    ) {
        return ResponseEntity.ok(
                supplierItemCapabilityService.updateCapability(
                        supplierPublicId,
                        itemPublicId,
                        request,
                        actorUserPublicId
                )
        );
    }
}
