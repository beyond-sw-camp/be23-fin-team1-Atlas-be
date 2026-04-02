package com.ozz.atlas.supply.item.controller;

import com.ozz.atlas.supply.item.dtos.CreateItemCategoryRequest;
import com.ozz.atlas.supply.item.dtos.ItemCategoryResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemCategoryRequest;
import com.ozz.atlas.supply.item.service.SupplyItemCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item-category")
public class SupplyItemCategoryController {

    private final SupplyItemCategoryService supplyItemCategoryService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemCategoryResponse createCategory(@Valid @RequestBody CreateItemCategoryRequest request) {
        return supplyItemCategoryService.createCategory(request);
    }

    @PutMapping("/{categoryId}")
    public ItemCategoryResponse updateCategory(@PathVariable Long categoryId,
                                               @Valid @RequestBody UpdateItemCategoryRequest request) {
        return supplyItemCategoryService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId) {
        supplyItemCategoryService.deleteCategory(categoryId);
    }

    @GetMapping("/{categoryId}")
    public ItemCategoryResponse getCategory(@PathVariable Long categoryId) {
        return supplyItemCategoryService.getCategory(categoryId);
    }

    @GetMapping
    public Page<ItemCategoryResponse> getCategoryList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return supplyItemCategoryService.getCategoryList(pageable);
    }

}
