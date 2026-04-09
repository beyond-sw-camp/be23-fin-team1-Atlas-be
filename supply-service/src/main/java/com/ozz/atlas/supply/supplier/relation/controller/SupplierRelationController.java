package com.ozz.atlas.supply.supplier.relation.controller;

import com.ozz.atlas.supply.supplier.relation.dtos.CreateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.dtos.UpdateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.service.SupplierRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers/{parentSupplierPublicId}/relations")
public class SupplierRelationController {

    private final SupplierRelationService supplierRelationService;

    @PostMapping
    public ResponseEntity<?> createRelation(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId,
            @Valid @RequestBody CreateSupplierRelationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierRelationService.createRelation(organizationPublicId, parentSupplierPublicId, request));
    }

    @GetMapping
    public ResponseEntity<?> getRelations(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId
    ) {
        return ResponseEntity.ok(
                supplierRelationService.getRelations(organizationPublicId, parentSupplierPublicId)
        );
    }

    @GetMapping("/{relationPublicId}")
    public ResponseEntity<?> getRelation(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId,
            @PathVariable String relationPublicId
    ) {
        return ResponseEntity.ok(
                supplierRelationService.getRelation(organizationPublicId, parentSupplierPublicId, relationPublicId)
        );
    }

    @PatchMapping("/{relationPublicId}")
    public ResponseEntity<?> updateRelation(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId,
            @PathVariable String relationPublicId,
            @Valid @RequestBody UpdateSupplierRelationRequest request
    ) {
        return ResponseEntity.ok(
                supplierRelationService.updateRelation(
                        organizationPublicId,
                        parentSupplierPublicId,
                        relationPublicId,
                        request
                )
        );
    }

    @DeleteMapping("/{relationPublicId}")
    public ResponseEntity<?> deleteRelation(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId,
            @PathVariable String relationPublicId
    ) {
        supplierRelationService.deleteRelation(organizationPublicId, parentSupplierPublicId, relationPublicId);
        return ResponseEntity.noContent().build();
    }
}
