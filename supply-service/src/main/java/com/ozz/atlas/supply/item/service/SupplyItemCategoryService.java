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
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemCategoryService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final SupplyItemCategoryRepository supplyItemCategoryRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final SupplierRepository supplierRepository;

    public ItemCategoryResponse createCategory(
            String organizationPublicId,
            String userPublicId,
            String userRole,
            CreateItemCategoryRequest request
    ) {
        validateUserHeader(userPublicId);
        validateCategoryCreateAuthority(organizationPublicId, userRole);

        SupplyItemCategory parentCategory = getParentCategory(request.getParentCategoryPublicId());
        int categoryLevel = parentCategory != null ? parentCategory.getCategoryLevel() + 1 : 1;

        SupplyItemCategory category = SupplyItemCategory.create(
                parentCategory,
                request.getCategoryName(),
                categoryLevel,
                request.getSortOrder() != null ? request.getSortOrder() : 1,
                organizationPublicId,
                userPublicId
        );

        return ItemCategoryResponse.fromEntity(supplyItemCategoryRepository.save(category));
    }

    public ItemCategoryResponse updateCategory(
            String categoryPublicId,
            String organizationPublicId,
            String userPublicId,
            String userRole,
            UpdateItemCategoryRequest request
    ) {
        validateUserHeader(userPublicId);

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        validateCategoryUpdateAuthority(category, organizationPublicId, userPublicId, userRole);

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

    public void deleteCategory(
            String categoryPublicId,
            String organizationPublicId,
            String userRole
    ) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        validateCategoryDeleteAuthority(category, organizationPublicId, userRole);

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        if (supplyItemRepository.existsByItemCategoryAndStatusIn(category, List.of(Status.ACTIVE, Status.DEACTIVE))) {
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

    private SupplyItemCategory getParentCategory(String parentCategoryPublicId) {
        if (parentCategoryPublicId == null || parentCategoryPublicId.isBlank()) {
            return null;
        }

        return supplyItemCategoryRepository.findByPublicIdAndStatus(parentCategoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.PARENT_CATEGORY_NOT_FOUND));
    }

    private void validateCategoryCreateAuthority(String organizationPublicId, String userRole) {
        if (isAdmin(userRole)) {
            return;
        }

        validateOrganizationHeader(organizationPublicId);

        if (!isMainCompanyOrganization(organizationPublicId)) {
            throw new ItemException(ItemErrorCode.CATEGORY_WRITE_FORBIDDEN);
        }
    }

    private void validateCategoryUpdateAuthority(
            SupplyItemCategory category,
            String organizationPublicId,
            String userPublicId,
            String userRole
    ) {
        if (isAdmin(userRole)) {
            return;
        }

        validateOrganizationHeader(organizationPublicId);

        if (!isMainCompanyOrganization(organizationPublicId)) {
            throw new ItemException(ItemErrorCode.ACCESS_DENIED);
        }

        if (!Objects.equals(category.getCreatedByOrganizationPublicId(), organizationPublicId)
                || !Objects.equals(category.getCreatedByUserPublicId(), userPublicId)) {
            throw new ItemException(ItemErrorCode.CATEGORY_UPDATE_FORBIDDEN);
        }
    }

    private void validateCategoryDeleteAuthority(
            SupplyItemCategory category,
            String organizationPublicId,
            String userRole
    ) {
        if (isAdmin(userRole)) {
            return;
        }

        validateOrganizationHeader(organizationPublicId);

        if (!isMainCompanyOrganization(organizationPublicId)) {
            throw new ItemException(ItemErrorCode.CATEGORY_WRITE_FORBIDDEN);
        }

        if (!Objects.equals(category.getCreatedByOrganizationPublicId(), organizationPublicId)) {
            throw new ItemException(ItemErrorCode.ACCESS_DENIED);
        }
    }

    private boolean isMainCompanyOrganization(String organizationPublicId) {
        return !supplierRepository.existsByOrganizationPublicId(organizationPublicId);
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private void validateUserHeader(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_ACTOR_HEADER);
        }
    }
}
