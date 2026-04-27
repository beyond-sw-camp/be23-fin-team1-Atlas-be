package com.ozz.atlas.supply.supplier.relation.controller;

import com.ozz.atlas.supply.supplier.relation.dtos.CreateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.dtos.UpdateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.service.SupplierRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/suppliers/{parentSupplierPublicId}/relations")
@Tag(name = "SupplierRelation")
public class SupplierRelationController {

    private final SupplierRelationService supplierRelationService;

    @Operation(summary = "협력사 관계 생성")
    @PostMapping
    public ResponseEntity<?> createRelation(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId,
            @Valid @RequestBody CreateSupplierRelationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierRelationService.createRelation(organizationPublicId, parentSupplierPublicId, request));
    }

    @Operation(summary = "협력사 관계 목록 조회")
    @GetMapping
    public ResponseEntity<?> getRelations(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String parentSupplierPublicId
    ) {
        return ResponseEntity.ok(
                supplierRelationService.getRelations(organizationPublicId, parentSupplierPublicId)
        );
    }

    @Operation(summary = "협력사 관계 상세 조회")
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

    @Operation(summary = "협력사 관계 수정")
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

    @Operation(summary = "협력사 관계 삭제")
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
