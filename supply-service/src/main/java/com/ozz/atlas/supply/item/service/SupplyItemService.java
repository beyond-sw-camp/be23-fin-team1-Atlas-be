package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
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
public class SupplyItemService {

    private final SupplyItemRepository supplyItemRepository;
    private final SupplyItemCategoryRepository supplyItemCategoryRepository;
    private final SupplierRepository supplierRepository;

    public ItemResponse createItem(String organizationPublicId, CreateItemRequest request) {
        SupplySupplier supplier = getManageableSupplier(organizationPublicId);

        if (supplyItemRepository.existsByItemCode(request.getItemCode())) {
            throw new ItemException(ItemErrorCode.ITEM_CODE_ALREADY_EXISTS);
        }

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(
                        request.getItemCategoryPublicId(),
                        Status.ACTIVE
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));

        SupplyItem item = SupplyItem.create(
                supplier,
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        return ItemResponse.fromEntity(supplyItemRepository.save(item));
    }

    public ItemResponse updateItem(
            String organizationPublicId,
            String itemPublicId,
            UpdateItemRequest request
    ) {
        SupplySupplier supplier = getManageableSupplier(organizationPublicId);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, supplier);

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(
                        request.getItemCategoryPublicId(),
                        Status.ACTIVE
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));

        if (supplyItemRepository.existsByItemCodeAndIdNot(request.getItemCode(), item.getId())) {
            throw new ItemException(ItemErrorCode.ITEM_CODE_ALREADY_EXISTS);
        }

        item.update(
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        return ItemResponse.fromEntity(item);
    }

    public void deleteItem(String organizationPublicId, String itemPublicId) {
        SupplySupplier supplier = getManageableSupplier(organizationPublicId);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, supplier);

        item.changeActiveYn(Status.DELETE);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItem(String itemPublicId) {
        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        return ItemResponse.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemList(Pageable pageable) {
        return supplyItemRepository.findAllByStatus(Status.ACTIVE, pageable)
                .map(ItemResponse::fromEntity);
    }

    private SupplySupplier getManageableSupplier(String organizationPublicId) {
        validateOrganizationHeader(organizationPublicId);

        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new ItemException(ItemErrorCode.SUPPLIER_NOT_FOUND));

        if (supplier.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ItemException(ItemErrorCode.SUPPLIER_NOT_APPROVED);
        }

        if (supplier.getSupplierStatus() != SupplierStatus.ACTIVE) {
            throw new ItemException(ItemErrorCode.SUPPLIER_NOT_ACTIVE);
        }

        return supplier;
    }

    private void validateItemOwner(SupplyItem item, SupplySupplier supplier) {
        if (!Objects.equals(item.getSupplier().getId(), supplier.getId())) {
            throw new ItemException(ItemErrorCode.ACCESS_DENIED);
        }
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_ACTOR_HEADER);
        }
    }
}
