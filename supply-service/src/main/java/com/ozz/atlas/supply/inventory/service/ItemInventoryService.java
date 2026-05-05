package com.ozz.atlas.supply.inventory.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.inventory.dtos.CreateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.dtos.ItemInventoryResponse;
import com.ozz.atlas.supply.inventory.dtos.ItemInventorySummaryResponse;
import com.ozz.atlas.supply.inventory.dtos.UpdateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryErrorCode;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryException;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyType;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemInventoryService {

    private final SupplyItemInventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository itemRepository;
    private final SupplierItemCapabilityRepository capabilityRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;

    public ItemInventoryResponse createInventory(
            String organizationPublicId,
            String organizationType,
            CreateItemInventoryRequest request
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplyItem item = getManagedItem(supplier, request.getItemPublicId());

        LogisticsNode logisticsNode = getActiveOwnedLogisticsNode(
                organizationPublicId,
                request.getLogisticsNodePublicId()
        );
        LocalDate expirationDate = request.getManufacturedDate()
                .plusDays(item.getShelfLifeDays());

        validateInventoryDates(request.getManufacturedDate(), expirationDate);

        SupplyItemInventory inventory = SupplyItemInventory.create(
                supplier,
                item,
                logisticsNode,
                request.getManufacturedDate(),
                expirationDate,
                request.getQty(),
                request.getMemo()
        );

        SupplyItemInventory saved = inventoryRepository.save(inventory);
        syncCapabilityAvailableQty(supplier, item);

        return ItemInventoryResponse.from(saved);
    }


    @Transactional(readOnly = true)
    public List<ItemInventoryResponse> getInventories(String organizationPublicId, String organizationType) {
        getWritableSupplier(organizationPublicId, organizationType);

        return inventoryRepository
                .findAllBySupplier_OrganizationPublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
                        organizationPublicId,
                        InventoryStatus.DELETED
                )
                .stream()
                .map(ItemInventoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemInventoryResponse getInventory(
            String organizationPublicId,
            String organizationType,
            String inventoryPublicId
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        return ItemInventoryResponse.from(getOwnedInventory(supplier, inventoryPublicId));
    }

    public ItemInventoryResponse updateInventory(
            String organizationPublicId,
            String organizationType,
            String inventoryPublicId,
            UpdateItemInventoryRequest request
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplyItemInventory inventory = getOwnedInventory(supplier, inventoryPublicId);

        LogisticsNode logisticsNode = getActiveOwnedLogisticsNode(
                organizationPublicId,
                request.getLogisticsNodePublicId()
        );
        LocalDate expirationDate = request.getManufacturedDate()
                .plusDays(inventory.getItem().getShelfLifeDays());

        validateInventoryDates(request.getManufacturedDate(), expirationDate);

        try {
            inventory.update(
                    logisticsNode,
                    request.getManufacturedDate(),
                    expirationDate,
                    request.getQty(),
                    request.getMemo()
            );
        } catch (IllegalStateException e) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_EDIT_NOT_ALLOWED);
        }

        syncCapabilityAvailableQty(supplier, inventory.getItem());

        return ItemInventoryResponse.from(inventory);
    }


    public void deleteInventory(String organizationPublicId, String organizationType, String inventoryPublicId) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplyItemInventory inventory = getOwnedInventory(supplier, inventoryPublicId);

        try {
            inventory.delete();
        } catch (IllegalStateException e) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_DELETE_NOT_ALLOWED);
        }

        syncCapabilityAvailableQty(supplier, inventory.getItem());
    }

    public void reserveConfirmedQty(SupplySupplier supplier, SupplyItem item, Long confirmQty) {
        validateAvailableQty(supplier, item, confirmQty);

        long remaining = confirmQty;

        List<SupplyItemInventory> inventories = inventoryRepository.findReservableForUpdate(
                supplier.getId(),
                item.getId(),
                List.of(InventoryStatus.ACTIVE, InventoryStatus.RESERVED),
                LocalDate.now()
        );

        for (SupplyItemInventory inventory : inventories) {
            if (remaining <= 0) {
                break;
            }

            long reserveQty = Math.min(inventory.getAvailableQty(), remaining);
            inventory.reserve(reserveQty);
            remaining -= reserveQty;
        }

        if (remaining > 0) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }

        syncCapabilityAvailableQty(supplier, item);
    }

    public void validateAvailableQty(SupplySupplier supplier, SupplyItem item, Long confirmQty) {
        Long availableQty = inventoryRepository.sumAvailableQty(supplier.getId(), item.getId(), LocalDate.now());

        if (availableQty < confirmQty) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }
    }

    public void syncAvailableQtyForShipment(SupplySupplier supplier, SupplyItem item) {
        syncCapabilityAvailableQty(supplier, item);
    }

    public void deductShipmentQty(SupplySupplier supplier, SupplyItem item, Long shipmentQty) {
        if (item.getSupplyType() == SupplyType.MAKE_TO_ORDER) {
            deductMakeToOrderShipmentQty(supplier, item, shipmentQty);
            return;
        }

        deductStockBasedShipmentQty(supplier, item, shipmentQty);
    }

    private void deductMakeToOrderShipmentQty(SupplySupplier supplier, SupplyItem item, Long shipmentQty) {
        long remaining = shipmentQty;

        List<SupplyItemInventory> inventories = inventoryRepository.findAvailableForDeductUpdate(
                supplier.getId(),
                item.getId(),
                List.of(InventoryStatus.ACTIVE, InventoryStatus.RESERVED),
                LocalDate.now()
        );

        for (SupplyItemInventory inventory : inventories) {
            if (remaining <= 0) {
                break;
            }

            long deductQty = Math.min(inventory.getRemainingQty(), remaining);
            inventory.deductRemainingOnly(deductQty);
            remaining -= deductQty;
        }

        if (remaining > 0) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }
    }


    private void deductStockBasedShipmentQty(SupplySupplier supplier, SupplyItem item, Long shipmentQty) {
        long remaining = shipmentQty;

        List<SupplyItemInventory> inventories = inventoryRepository.findReservedForUpdate(
                supplier.getId(),
                item.getId(),
                LocalDate.now()
        );

        for (SupplyItemInventory inventory : inventories) {
            if (remaining <= 0) {
                break;
            }

            long deductQty = Math.min(inventory.getReservedQty(), remaining);
            inventory.deductReserved(deductQty);
            remaining -= deductQty;
        }

        if (remaining > 0) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }

        syncCapabilityAvailableQty(supplier, item);
    }

    private SupplySupplier getWritableSupplier(String organizationPublicId, String organizationType) {
        if (!"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVALID_INPUT_VALUE);
        }

        return supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .filter(supplier -> supplier.getSupplierStatus() != SupplierStatus.TERMINATED)
                .orElseThrow(() -> new ItemInventoryException(ItemInventoryErrorCode.SUPPLIER_NOT_FOUND));
    }

    private SupplyItem getManagedItem(SupplySupplier supplier, String itemPublicId) {
        SupplyItem item = itemRepository.findByPublicIdAndStatusIn(itemPublicId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new ItemInventoryException(ItemInventoryErrorCode.ITEM_NOT_FOUND));

        if (!item.getSupplier().getId().equals(supplier.getId())) {
            throw new ItemInventoryException(ItemInventoryErrorCode.ITEM_NOT_FOUND);
        }

        return item;
    }

    private SupplyItemInventory getOwnedInventory(SupplySupplier supplier, String inventoryPublicId) {
        SupplyItemInventory inventory = inventoryRepository
                .findByPublicIdAndStatusNot(inventoryPublicId, InventoryStatus.DELETED)
                .orElseThrow(() -> new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_NOT_FOUND));

        if (!inventory.getSupplier().getId().equals(supplier.getId())) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_NOT_FOUND);
        }

        return inventory;
    }

    private void validateInventoryDates(LocalDate manufacturedDate, LocalDate expirationDate) {
        if (manufacturedDate == null || expirationDate == null || expirationDate.isBefore(manufacturedDate)) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void syncCapabilityAvailableQty(SupplySupplier supplier, SupplyItem item) {
        if (item.getSupplyType() != SupplyType.STOCK_BASED) {
            return;
        }

        Long availableQty = inventoryRepository.sumAvailableQty(
                supplier.getId(),
                item.getId(),
                LocalDate.now()
        );

        SupplySupplierItemCapability capability = capabilityRepository
                .findBySupplier_IdAndItem_Id(supplier.getId(), item.getId())
                .orElse(null);

        if (capability != null) {
            capability.syncAvailableQty(availableQty);
        }
    }

    @Transactional(readOnly = true)
    public ItemInventorySummaryResponse getInventorySummary(
            String organizationPublicId,
            String organizationType
    ) {
        getWritableSupplier(organizationPublicId, organizationType);

        Long remainingQty = inventoryRepository.sumRemainingQtyByOrganizationPublicId(organizationPublicId);
        Long reservedQty = inventoryRepository.sumReservedQtyByOrganizationPublicId(organizationPublicId);
        Long availableQty = inventoryRepository.sumAvailableQtyByOrganizationPublicId(
                organizationPublicId,
                LocalDate.now()
        );

        return ItemInventorySummaryResponse.of(remainingQty, reservedQty, availableQty);
    }

    @Transactional(readOnly = true)
    public List<ItemInventoryResponse> getItemInventories(
            String organizationPublicId,
            String organizationType,
            String itemPublicId
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        getManagedItem(supplier, itemPublicId);

        return inventoryRepository
                .findAllBySupplier_OrganizationPublicIdAndItem_PublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
                        organizationPublicId,
                        itemPublicId,
                        InventoryStatus.DELETED
                )
                .stream()
                .map(ItemInventoryResponse::from)
                .toList();
    }
    private LogisticsNode getActiveOwnedLogisticsNode(String organizationPublicId, String logisticsNodePublicId) {
        return logisticsNodeRepository
                .findByPublicIdAndOrganizationPublicIdAndActiveTrue(logisticsNodePublicId, organizationPublicId)
                .orElseThrow(() -> new ItemInventoryException(ItemInventoryErrorCode.INVALID_INPUT_VALUE));
    }

    @Operation(summary = "물류 거점 재고 목록 조회")
    @Transactional(readOnly = true)
    public List<ItemInventoryResponse> getNodeInventories(
            String organizationPublicId,
            String organizationType,
            String nodePublicId
    ) {
        getWritableSupplier(organizationPublicId, organizationType);
        getOwnedWarehouse(organizationPublicId, nodePublicId);

        return inventoryRepository
                .findAllBySupplier_OrganizationPublicIdAndLogisticsNode_PublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
                        organizationPublicId,
                        nodePublicId,
                        InventoryStatus.DELETED
                )
                .stream()
                .map(ItemInventoryResponse::from)
                .toList();
    }

    private LogisticsNode getOwnedWarehouse(String organizationPublicId, String nodePublicId) {
        return logisticsNodeRepository
                .findByPublicIdAndOrganizationPublicIdAndNodeType(
                        nodePublicId,
                        organizationPublicId,
                        LogisticsNodeType.WAREHOUSE
                )
                .orElseThrow(() -> new ItemInventoryException(ItemInventoryErrorCode.LOGISTICS_NODE_NOT_FOUND));
    }




}
