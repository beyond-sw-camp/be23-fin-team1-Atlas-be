package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.ChangeItemCategoryStatusRequest;
import com.ozz.atlas.supply.item.dtos.CreateItemCategoryRequest;
import com.ozz.atlas.supply.item.dtos.ItemCategoryResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemCategoryRequest;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityErrorCode;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemCategoryService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String BUYER_ORGANIZATION_TYPE = "BUYER";
    private static final List<Status> MANAGED_CATEGORY_STATUSES = List.of(Status.ACTIVE, Status.DEACTIVE);

    private final SupplyItemCategoryRepository supplyItemCategoryRepository;
    private final SupplyItemRepository supplyItemRepository;

    public ItemCategoryResponse createCategory(
            String organizationPublicId,
            String organizationType,
            String userRole,
            CreateItemCategoryRequest request
    ) {
        validateCategoryWriteAuthority(organizationPublicId, organizationType, userRole);

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
                request.getSortOrder() != null ? request.getSortOrder() : 1,
                organizationPublicId
        );

        return ItemCategoryResponse.fromEntity(supplyItemCategoryRepository.save(category));
    }

    public ItemCategoryResponse updateCategory(
            String categoryPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole,
            UpdateItemCategoryRequest request
    ) {
        // 카테고리 수정 권한이 있는 조직/역할인지 먼저 검증
        validateCategoryWriteAuthority(organizationPublicId, organizationType, userRole);

        // 수정 대상 카테고리를 활성 상태(Status.ACTIVE) 기준으로 조회
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        // 조회한 카테고리가 현재 사용자 조직 소유인지 검증
        validateCategoryOwner(category, organizationPublicId, userRole);

        // 하위 카테고리가 하나라도 있으면 수정 불가
        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        SupplyItemCategory parentCategory = null;
        int categoryLevel = 1;

        // 요청에 부모 카테고리 publicId가 들어온 경우 부모 카테고리 변경 처리
        if (request.getParentCategoryPublicId() != null && !request.getParentCategoryPublicId().isBlank()) {
            // 자기 자신을 부모 카테고리로 지정하는 것은 불가
            if (request.getParentCategoryPublicId().equals(category.getPublicId())) {
                throw new ItemException(ItemErrorCode.CATEGORY_SELF_PARENT);
            }

            // 새 부모 카테고리를 활성 상태 기준으로 조회
            parentCategory = supplyItemCategoryRepository.findByPublicIdAndStatus(
                            request.getParentCategoryPublicId(),
                            Status.ACTIVE
                    )
                    .orElseThrow(() -> new ItemException(ItemErrorCode.PARENT_CATEGORY_NOT_FOUND));
            // 부모 카테고리 레벨 + 1로 현재 카테고리 레벨 재계산
            categoryLevel = parentCategory.getCategoryLevel() + 1;
        }

        // 카테고리 정보 수정
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
            String organizationType,
            String userRole
    ) {
        validateCategoryWriteAuthority(organizationPublicId, organizationType, userRole);

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        validateCategoryOwner(category, organizationPublicId, userRole);

        if (supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        if (supplyItemRepository.existsByItemCategoryAndStatusIn(category, List.of(Status.ACTIVE, Status.DEACTIVE))) {
            throw new ItemException(ItemErrorCode.ITEM_EXISTS_IN_CATEGORY);
        }

        category.changeActiveYn(Status.DELETE);
    }

    public ItemCategoryResponse changeCategoryStatus(
            String categoryPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole,
            ChangeItemCategoryStatusRequest request
    ) {
        validateCategoryWriteAuthority(organizationPublicId, organizationType, userRole);

        if (request.getStatus() != Status.ACTIVE && request.getStatus() != Status.DEACTIVE) {
            throw new ItemException(ItemErrorCode.INVALID_INPUT_VALUE);
        }

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatusIn(
                        categoryPublicId,
                        MANAGED_CATEGORY_STATUSES
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        validateCategoryOwner(category, organizationPublicId, userRole);

        if (request.getStatus() == Status.DEACTIVE
                && supplyItemCategoryRepository.existsByParentCategory_IdAndStatus(category.getId(), Status.ACTIVE)) {
            throw new ItemException(ItemErrorCode.CATEGORY_CHILD_EXISTS);
        }

        if (request.getStatus() == Status.ACTIVE
                && category.getParentCategory() != null
                && category.getParentCategory().getStatus() != Status.ACTIVE) {
            throw new ItemException(ItemErrorCode.PARENT_CATEGORY_NOT_FOUND);
        }

        category.changeActiveYn(request.getStatus());
        return ItemCategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public ItemCategoryResponse getCategory(String categoryPublicId) {
        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(categoryPublicId, Status.ACTIVE)
                .orElseThrow(() -> new ItemException(ItemErrorCode.CATEGORY_NOT_FOUND));

        return ItemCategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public Page<ItemCategoryResponse> getCategoryList(Pageable pageable) {
        return supplyItemCategoryRepository.findAllByStatusIn(MANAGED_CATEGORY_STATUSES, pageable)
                .map(ItemCategoryResponse::fromEntity);
    }

    private void validateCategoryWriteAuthority(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateOrganizationHeader(organizationPublicId);

        if (isAdmin(userRole)) {
            return;
        }

        if (!BUYER_ORGANIZATION_TYPE.equals(organizationType)) {
            throw new ItemException(ItemErrorCode.CATEGORY_WRITE_FORBIDDEN);
        }
    }

    private void validateCategoryOwner(
            SupplyItemCategory category,
            String organizationPublicId,
            String userRole
    ) {
        if (isAdmin(userRole)) {
            return;
        }

        if (!category.getCreatedByOrganizationPublicId().equals(organizationPublicId)) {
            throw new ItemException(ItemErrorCode.CATEGORY_OWNER_FORBIDDEN);
        }
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private SupplyItem getActiveItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.ITEM_NOT_FOUND));
    }
}
