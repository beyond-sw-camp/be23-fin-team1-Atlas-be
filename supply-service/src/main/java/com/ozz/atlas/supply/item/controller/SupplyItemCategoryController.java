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
@RequestMapping("/item-category")
public class SupplyItemCategoryController {

    private final SupplyItemCategoryService supplyItemCategoryService;

//    품목 카테고리 등록
    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateItemCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyItemCategoryService.createCategory(request));
    }

//    품목 카테고리 수정
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
                                               @Valid @RequestBody UpdateItemCategoryRequest request) {
        return ResponseEntity.ok(supplyItemCategoryService.updateCategory(categoryId, request));
    }

//    품목 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        supplyItemCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

//    품목 카테고리 단건 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategory(categoryId));
    }

//    품목 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<?> getCategoryList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(supplyItemCategoryService.getCategoryList(pageable));
    }

}
