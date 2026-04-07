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

        if (request.getParentCategoryPublicId() != null && !request.getParentCategoryPublicId().isBlank()) {
            parentCategory = supplyItemCategoryRepository.findByPublicIdAndStatus(
                            request.getParentCategoryPublicId(),
                            Status.ACTIVE
                    )
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

    public ItemCategoryResponse updateCategory(String categoryPublicId, UpdateItemCategoryRequest request) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        SupplyItemCategory parentCategory = null;
        int categoryLevel = 1;

        if (request.getParentCategoryPublicId() != null && !request.getParentCategoryPublicId().isBlank()) {
            if (request.getParentCategoryPublicId().equals(category.getPublicId())) {
                throw new ItemException(ItemErrorCode.CATEGORY_SELF_PARENT);
            }

            parentCategory = supplyItemCategoryRepository.findByPublicIdAndStatus(
                            request.getParentCategoryPublicId(),
                            Status.ACTIVE
                    )
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

    public void deleteCategory(String categoryPublicId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        if (supplyItemRepository.existsByItemCategoryAndStatus(category, Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.ITEM_EXISTS_IN_CATEGORY);
        }

        category.changeActiveYn(Status.DELETE);
    }

    @Transactional(readOnly = true)
    public ItemCategoryResponse getCategory(String categoryPublicId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        return ItemCategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public Page<ItemCategoryResponse> getCategoryList(Pageable pageable) {
        return supplyItemCategoryRepository.findAllByStatus(Status.ACTIVE, pageable)
                .map(ItemCategoryResponse::fromEntity);
    }
}
