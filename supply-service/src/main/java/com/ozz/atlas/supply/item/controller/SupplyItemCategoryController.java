package com.ozz.atlas.supply.item.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/item-category")
public class SupplyItemCategoryController {

    private final SupplyItemCategoryService supplyItemCategoryService;

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

    @GetMapping("/{categoryPublicId}")
    public ResponseEntity<?> getCategory(@PathVariable String categoryPublicId) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategory(categoryPublicId));
    }

    @GetMapping
    public ResponseEntity<?> getCategoryList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategoryList(pageable));
    }
}
