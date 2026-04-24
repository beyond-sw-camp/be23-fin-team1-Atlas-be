package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.common.code.SequenceCodeType;
import com.ozz.atlas.supply.common.code.YearlySequenceCodeGenerator;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.relation.service.SupplierRelationService;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.item.search.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemService {

    private static final String SUPPLIER_ORGANIZATION_TYPE = "SUPPLIER";

    private final SupplyItemRepository supplyItemRepository;
    private final SupplyItemCategoryRepository supplyItemCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final ItemSearchService itemSearchService;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final SupplierRelationService supplierRelationService;

    public ItemResponse createItem(
            String organizationPublicId,
            String organizationType,
            CreateItemRequest request
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(
                        request.getItemCategoryPublicId(),
                        Status.ACTIVE
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));

        SupplyItem item = SupplyItem.create(
                supplier,
                category,
                generateNextItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getUnitPrice(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        SupplyItem savedItem = supplyItemRepository.save(item);
        itemSearchService.saveItemDocument(savedItem);
        return ItemResponse.fromEntity(savedItem);
    }

    public ItemResponse updateItem(
            String organizationPublicId,
            String organizationType,
            String itemPublicId,
            UpdateItemRequest request
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, organizationPublicId);

        SupplyItemCategory category = supplyItemCategoryRepository.findByPublicIdAndStatus(
                        request.getItemCategoryPublicId(),
                        Status.ACTIVE
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));


        item.update(
                category,
                request.getItemName(),
                request.getUnit(),
                request.getUnitPrice(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        itemSearchService.saveItemDocument(item);
        return ItemResponse.fromEntity(item);
    }

    public void deleteItem(
            String organizationPublicId,
            String organizationType,
            String itemPublicId
    ) {
        getWritableSupplier(organizationPublicId, organizationType);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, organizationPublicId);
        item.changeActiveYn(Status.DELETE);
        itemSearchService.saveItemDocument(item);
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

    private SupplySupplier getWritableSupplier(
            String organizationPublicId,
            String organizationType
    ) {
        validateOrganizationHeader(organizationPublicId);

        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            throw new ItemException(ItemErrorCode.INVALID_ORGANIZATION_TYPE);
        }

        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new ItemException(ItemErrorCode.SUPPLIER_NOT_FOUND));

        if (supplier.getSupplierStatus() != SupplierStatus.ACTIVE) {
            throw new ItemException(ItemErrorCode.SUPPLIER_NOT_ACTIVE);
        }

        return supplier;
    }

    private void validateItemOwner(SupplyItem item, String organizationPublicId) {
        if (!item.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new ItemException(ItemErrorCode.ACCESS_DENIED);
        }
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    @Transactional(readOnly = true)
    public void validateItemListAccess(
            String organizationPublicId,
            String organizationType,
            String supplierPublicId,
            String supplierOrganizationPublicId
    ) {
        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            return;
        }

        if ((supplierPublicId == null || supplierPublicId.isBlank())
                && (supplierOrganizationPublicId == null || supplierOrganizationPublicId.isBlank())) {
            return;
        }

        SupplySupplier loginSupplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplySupplier targetSupplier = resolveTargetSupplier(supplierPublicId, supplierOrganizationPublicId);

        if (loginSupplier.getId().equals(targetSupplier.getId())) {
            throw new ItemException(ItemErrorCode.SELF_SUPPLIER_ORDER_NOT_ALLOWED);
        }

        if (!supplierRelationService.hasVisibleRelation(loginSupplier.getId(), targetSupplier.getId())) {
            throw new ItemException(ItemErrorCode.ACCESS_DENIED);
        }
    }

    private SupplySupplier resolveTargetSupplier(
            String supplierPublicId,
            String supplierOrganizationPublicId
    ) {
        if (supplierPublicId != null && !supplierPublicId.isBlank()) {
            return supplierRepository.findByPublicIdAndSupplierStatusNot(
                            supplierPublicId,
                            SupplierStatus.TERMINATED
                    )
                    .orElseThrow(() -> new ItemException(ItemErrorCode.SUPPLIER_NOT_FOUND));
        }

        return supplierRepository.findByOrganizationPublicId(supplierOrganizationPublicId)
                .filter(supplier -> supplier.getSupplierStatus() != SupplierStatus.TERMINATED)
                .orElseThrow(() -> new ItemException(ItemErrorCode.SUPPLIER_NOT_FOUND));
    }

    private String generateNextItemCode() {
        String prefix = YearlySequenceCodeGenerator.currentPrefix(SequenceCodeType.ITEM);
        String lastCode = supplyItemRepository.findTopByItemCodeStartingWithOrderByItemCodeDesc(prefix)
                .map(SupplyItem::getItemCode)
                .orElse(null);

        String candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.ITEM, lastCode, 7);
        while (supplyItemRepository.existsByItemCode(candidate)) {
            candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.ITEM, candidate, 7);
        }
        return candidate;
    }



}
