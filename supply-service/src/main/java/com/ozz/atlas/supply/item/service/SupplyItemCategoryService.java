package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemCategoryRequest;
import com.ozz.atlas.supply.item.dtos.ItemCategoryResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemCategoryRequest;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemCategoryService {

    private final SupplyItemCategoryRepository supplyItemCategoryRepository;
    private final SupplyItemRepository supplyItemRepository;

    public ItemCategoryResponse createCategory(CreateItemCategoryRequest request) {
        SupplyItemCategory parentCategory = null;
        int categoryLevel = 1;

        if (request.getParentCategoryId() != null) {
            parentCategory = supplyItemCategoryRepository.findByIdAndActiveYn(request.getParentCategoryId(), 1)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
            categoryLevel = parentCategory.getCategoryLevel() + 1;
        }

        SupplyItemCategory category = SupplyItemCategory.create(
                parentCategory,
                request.getCategoryName(),
                categoryLevel,
                request.getSortOrder() != null ? request.getSortOrder() : 1
        );

        return ItemCategoryResponse.from(supplyItemCategoryRepository.save(category));
    }

    public ItemCategoryResponse updateCategory(Long categoryId, UpdateItemCategoryRequest request) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndActiveYn(categoryId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        SupplyItemCategory parentCategory = null;
        int categoryLevel = 1;
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(categoryId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category cannot be its own parent");
            }
            parentCategory = supplyItemCategoryRepository.findByIdAndActiveYn(request.getParentCategoryId(), 1)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
            categoryLevel = parentCategory.getCategoryLevel() + 1;
        }

        category.update(
                parentCategory,
                request.getCategoryName(),
                categoryLevel,
                request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder()
        );

        return ItemCategoryResponse.from(category);
    }

    public void deleteCategory(Long categoryId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndActiveYn(categoryId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndActiveYn(categoryId, 1)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Child category exists");
        }

        if (supplyItemRepository.existsByItemCategoryAndActiveYn(category, 1)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Items exist in this category");
        }

        category.changeActiveYn(0);
    }
    @Transactional(readOnly = true)
    public ItemCategoryResponse getCategory(Long categoryId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndActiveYn(categoryId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return ItemCategoryResponse.from(category);
    }

    @Transactional(readOnly = true)
    public Page<ItemCategoryResponse> getCategories(Pageable pageable) {
        Page<SupplyItemCategory> categoryPage = supplyItemCategoryRepository.findAllByActiveYn(1, pageable);
        return categoryPage.map(ItemCategoryResponse::from);
    }
}
