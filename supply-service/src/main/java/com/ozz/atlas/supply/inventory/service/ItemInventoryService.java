package com.ozz.atlas.supply.inventory.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.inventory.dtos.CreateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.dtos.ItemInventoryResponse;
import com.ozz.atlas.supply.inventory.dtos.UpdateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryErrorCode;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryException;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
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

    public ItemInventoryResponse createInventory(
            String organizationPublicId,
            String organizationType,
            CreateItemInventoryRequest request
    ) {
        SupplySupplier supplier = getWritableSupplier(organizationPublicId, organizationType);
        SupplyItem item = getManagedItem(supplier, request.getItemPublicId());
        validateInventoryDates(request.getManufacturedDate(), request.getExpirationDate());

        SupplyItemInventory inventory = SupplyItemInventory.create(
                supplier,
                item,
                request.getManufacturedDate(),
                request.getExpirationDate(),
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
        validateInventoryDates(request.getManufacturedDate(), request.getExpirationDate());

        try {
            inventory.update(
                    request.getManufacturedDate(),
                    request.getExpirationDate(),
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
        Long availableQty = inventoryRepository.sumAvailableQty(supplier.getId(), item.getId(), LocalDate.now());

        if (availableQty < confirmQty) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }

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

    public void deductShipmentQty(SupplySupplier supplier, SupplyItem item, Long shipmentQty) {
        long remaining = shipmentQty;

        List<SupplyItemInventory> inventories = inventoryRepository.findReservedForUpdate(
                supplier.getId(),
                item.getId()
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
        Long availableQty = inventoryRepository.sumAvailableQty(supplier.getId(), item.getId(), LocalDate.now());

        SupplySupplierItemCapability capability = capabilityRepository
                .findBySupplier_IdAndItem_Id(supplier.getId(), item.getId())
                .orElse(null);

        if (capability != null) {
            capability.syncAvailableQty(availableQty);
        }
    }
}
