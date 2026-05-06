package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.common.code.SequenceCodeType;
import com.ozz.atlas.supply.common.code.YearlySequenceCodeGenerator;
import com.ozz.atlas.supply.item.client.FileServiceClient;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.*;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderItemRepository;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
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
    private static final List<Status> MANAGED_ITEM_STATUSES = List.of(Status.ACTIVE, Status.DEACTIVE);
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final SupplierItemCapabilityRepository supplierItemCapabilityRepository;
    private final FileServiceClient fileServiceClient;



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

        LogisticsNode originLogisticsNode = resolveOriginLogisticsNode(
                organizationPublicId,
                request.getOriginLogisticsNodePublicId()
        );

        SupplyItem item = SupplyItem.create(
                supplier,
                category,
                originLogisticsNode,
                generateNextItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getUnitPrice(),
                request.getSpec(),
                request.getShelfLifeDays(),
                request.getSupplyType()
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


        LogisticsNode originLogisticsNode = resolveOriginLogisticsNode(
                organizationPublicId,
                request.getOriginLogisticsNodePublicId()
        );

        item.update(
                category,
                originLogisticsNode,
                request.getItemName(),
                request.getUnit(),
                request.getUnitPrice(),
                request.getSpec(),
                request.getShelfLifeDays(),
                request.getSupplyType()
        );

        itemSearchService.saveItemDocument(item);
        return toItemResponseWithCapability(item);
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

        return toItemResponseWithCapability(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemList(Pageable pageable) {
        return supplyItemRepository.findAllByStatus(Status.ACTIVE, pageable)
                .map(this::toItemResponseWithCapability);
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
            String supplierOrganizationPublicId,
            Status status
    ) {
        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            return;
        }

        if ((supplierPublicId == null || supplierPublicId.isBlank())
                && (supplierOrganizationPublicId == null || supplierOrganizationPublicId.isBlank())) {
            return;
        }

        if (status == null || status == Status.ACTIVE) {
            return;
        }

        SupplySupplier loginSupplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplySupplier targetSupplier = resolveTargetSupplier(supplierPublicId, supplierOrganizationPublicId);

        if (loginSupplier.getId().equals(targetSupplier.getId())) {
            return;
        }

        if (supplierRelationService.hasVisibleRelation(loginSupplier.getId(), targetSupplier.getId())) {
            return;
        }

        throw new ItemException(ItemErrorCode.ACCESS_DENIED);

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

    @Transactional(readOnly = true)
    public Page<ItemResponse> getManagedItemList(
            String organizationPublicId,
            String organizationType,
            Pageable pageable
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);

        return supplyItemRepository.findAllBySupplier_OrganizationPublicIdAndStatusIn(
                supplier.getOrganizationPublicId(),
                MANAGED_ITEM_STATUSES,
                pageable
        ).map(this::toItemResponseWithCapability);
    }

    @Transactional(readOnly = true)
    public ItemDashboardSummaryResponse getManagedItemDashboard(
            String organizationPublicId,
            String organizationType
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        String supplierOrgId = supplier.getOrganizationPublicId();

        var today = java.time.LocalDate.now(KST);
        var from = today.atStartOfDay();
        var to = today.plusDays(1).atStartOfDay();

        return ItemDashboardSummaryResponse.builder()
                .totalItemCount(
                        supplyItemRepository.countBySupplier_OrganizationPublicIdAndStatusIn(
                                supplierOrgId,
                                MANAGED_ITEM_STATUSES
                        )
                )
                .activeItemCount(
                        supplyItemRepository.countBySupplier_OrganizationPublicIdAndStatus(
                                supplierOrgId,
                                Status.ACTIVE
                        )
                )
                .deactiveItemCount(
                        supplyItemRepository.countBySupplier_OrganizationPublicIdAndStatus(
                                supplierOrgId,
                                Status.DEACTIVE
                        )
                )
                .todayOrderedItemCount(
                        purchaseOrderItemRepository.countDistinctOrderedItemsToday(
                                supplierOrgId,
                                from,
                                to
                        )
                )
                .build();
    }

    @Transactional(readOnly = true)
    public List<ItemLinkedPurchaseOrderResponse> getManagedItemLinkedOrders(
            String organizationPublicId,
            String organizationType,
            String itemPublicId
    ) {
        getWritableSupplier(organizationPublicId, organizationType);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(itemPublicId, MANAGED_ITEM_STATUSES)
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, organizationPublicId);
        return purchaseOrderItemRepository.findLinkedOrdersByItemPublicId(itemPublicId);
    }

    public ItemResponse changeItemStatus(
            String organizationPublicId,
            String organizationType,
            String itemPublicId,
            ChangeItemStatusRequest request
    ) {
        getWritableSupplier(organizationPublicId, organizationType);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        List.of(Status.ACTIVE, Status.DEACTIVE)
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, organizationPublicId);

        if (request.getStatus() != Status.ACTIVE && request.getStatus() != Status.DEACTIVE) {
            throw new ItemException(ItemErrorCode.INVALID_INPUT_VALUE);
        }

        item.changeActiveYn(request.getStatus());
        itemSearchService.saveItemDocument(item);
        return toItemResponseWithCapability(item);
    }

    public ItemResponse changePrimaryMedia(
            String organizationPublicId,
            String organizationType,
            String itemPublicId,
            String filePublicId
    ) {
        getWritableSupplier(organizationPublicId, organizationType);

        SupplyItem item = supplyItemRepository.findByPublicIdAndStatusIn(
                        itemPublicId,
                        MANAGED_ITEM_STATUSES
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        validateItemOwner(item, organizationPublicId);
        fileServiceClient.getItemImageFile(itemPublicId, filePublicId);

        item.changePrimaryMedia(filePublicId);
        itemSearchService.saveItemDocument(item);
        return toItemResponseWithCapability(item);
    }

    private LogisticsNode resolveOriginLogisticsNode(
            String organizationPublicId,
            String originLogisticsNodePublicId
    ) {
        if (originLogisticsNodePublicId == null || originLogisticsNodePublicId.isBlank()) {
            throw new ItemException(ItemErrorCode.INVALID_INPUT_VALUE);
        }

        return logisticsNodeRepository.findByPublicIdAndOrganizationPublicIdAndActiveTrue(
                        originLogisticsNodePublicId,
                        organizationPublicId
                )
                .orElseThrow(() -> new ItemException(ItemErrorCode.INVALID_INPUT_VALUE));
    }

    private ItemResponse toItemResponseWithCapability(SupplyItem item) {
        SupplySupplierItemCapability capability = supplierItemCapabilityRepository
                .findBySupplier_IdAndItem_Id(
                        item.getSupplier().getId(),
                        item.getId()
                )
                .orElse(null);

        return ItemResponse.fromEntityWithCapability(item, capability);
    }





}
