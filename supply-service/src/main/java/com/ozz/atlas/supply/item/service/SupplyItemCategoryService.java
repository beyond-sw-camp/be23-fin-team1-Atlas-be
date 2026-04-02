package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemCategoryRequest;
import com.ozz.atlas.supply.item.dtos.ItemCategoryResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemCategoryRequest;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            parentCategory = supplyItemCategoryRepository.findByIdAndStatus(request.getParentCategoryId(), Status.ACTIVE)
                    .orElseThrow(() -> new ItemException(ItemErrorCode.PARENT_CATEGORY_NOT_FOUND));
            categoryLevel = parentCategory.getCategoryLevel() + 1;
        }

        SupplyItemCategory category = SupplyItemCategory.create(
                parentCategory,
                request.getCategoryName(),
                categoryLevel,
                request.getSortOrder() != null ? request.getSortOrder() : 1
        );

        return ItemCategoryResponse.fromEntity(supplyItemCategoryRepository.save(category));
    }

    public ItemCategoryResponse updateCategory(Long categoryId, UpdateItemCategoryRequest request) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndStatus(categoryId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(categoryId, Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        SupplyItemCategory parentCategory = null;
        int categoryLevel = 1;
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(categoryId)) {
                throw new ItemException(ItemErrorCode.CATEGORY_SELF_PARENT);
            }
            parentCategory = supplyItemCategoryRepository.findByIdAndStatus(request.getParentCategoryId(), Status.ACTIVE)
                    .orElseThrow(() -> new ItemException(ItemErrorCode.PARENT_CATEGORY_NOT_FOUND));
            categoryLevel = parentCategory.getCategoryLevel() + 1;
        }

        category.update(
                parentCategory,
                request.getCategoryName(),
                categoryLevel,
                request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder()
        );

        return ItemCategoryResponse.fromEntity(category);
    }

    public void deleteCategory(Long categoryId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndStatus(categoryId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(categoryId, Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        if (supplyItemRepository.existsByItemCategoryAndStatus(category, Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.ITEM_EXISTS_IN_CATEGORY);
        }

        category.changeActiveYn(Status.DELETE);
    }

    @Transactional(readOnly = true)
    public ItemCategoryResponse getCategory(Long categoryId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByIdAndStatus(categoryId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));
        return ItemCategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public Page<ItemCategoryResponse> getCategoryList(Pageable pageable) {
        Page<SupplyItemCategory> categoryPage = supplyItemCategoryRepository.findAllByStatus(Status.ACTIVE, pageable);
        return categoryPage.map(ItemCategoryResponse::fromEntity);
    }
}
