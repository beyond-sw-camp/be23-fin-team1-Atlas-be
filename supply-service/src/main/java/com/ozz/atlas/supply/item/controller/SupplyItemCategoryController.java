package com.ozz.atlas.supply.item.controller;

import com.ozz.atlas.supply.item.dtos.ChangeItemCategoryStatusRequest;
import com.ozz.atlas.supply.item.dtos.CreateItemCategoryRequest;
import com.ozz.atlas.supply.item.dtos.UpdateItemCategoryRequest;
import com.ozz.atlas.supply.item.service.SupplyItemCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/item-category")
@Tag(name = "SupplyItemCategory", description = "공급 품목 카테고리 생성, 조회, 수정, 삭제 API")
public class SupplyItemCategoryController {

    private final SupplyItemCategoryService supplyItemCategoryService;

    @Operation(summary = "품목 카테고리 생성")
    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateItemCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyItemCategoryService.createCategory(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        request
                ));
    }

    @Operation(summary = "품목 카테고리 수정")
    @PutMapping("/{categoryPublicId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable String categoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody UpdateItemCategoryRequest request
    ) {
        return ResponseEntity.ok(
                supplyItemCategoryService.updateCategory(
                        categoryPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole,
                        request
                )
        );
    }

    @Operation(summary = "품목 카테고리 삭제")
    @DeleteMapping("/{categoryPublicId}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable String categoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        supplyItemCategoryService.deleteCategory(
                categoryPublicId,
                organizationPublicId,
                organizationType,
                userRole
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "품목 카테고리 상태 변경")
    @PatchMapping("/{categoryPublicId}/status")
    public ResponseEntity<?> changeCategoryStatus(
            @PathVariable String categoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody ChangeItemCategoryStatusRequest request
    ) {
        return ResponseEntity.ok(
                supplyItemCategoryService.changeCategoryStatus(
                        categoryPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole,
                        request
                )
        );
    }

    @Operation(summary = "품목 카테고리 상세 조회")
    @GetMapping("/{categoryPublicId}")
    public ResponseEntity<?> getCategory(@PathVariable String categoryPublicId) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategory(categoryPublicId));
    }

    @Operation(summary = "품목 카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<?> getCategoryList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategoryList(pageable));
    }
}
