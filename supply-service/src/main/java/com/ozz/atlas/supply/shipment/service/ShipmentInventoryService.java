package com.ozz.atlas.supply.shipment.service;

import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryErrorCode;
import com.ozz.atlas.supply.inventory.exception.ItemInventoryException;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.supply.inventory.service.ItemInventoryService;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.shipment.domain.ShipmentInventoryAllocation;
import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import com.ozz.atlas.supply.shipment.repository.ShipmentInventoryAllocationRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ShipmentInventoryService {

    private final SupplyItemInventoryRepository inventoryRepository;
    private final ShipmentInventoryAllocationRepository allocationRepository;
    private final ItemInventoryService itemInventoryService;

    public ShipmentInventoryService(
            SupplyItemInventoryRepository inventoryRepository,
            ShipmentInventoryAllocationRepository allocationRepository,
            ItemInventoryService itemInventoryService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.allocationRepository = allocationRepository;
        this.itemInventoryService = itemInventoryService;
    }

    public Long getReservedQtyByNode(SupplySupplier supplier, SupplyItem item, Long logisticsNodeId) {
        return inventoryRepository.sumReservedQtyByNode(
                supplier.getId(),
                item.getId(),
                logisticsNodeId,
                LocalDate.now()
        );
    }

    public void deductReservedForShipmentLine(
            SupplySupplier supplier,
            SupplyItem item,
            Long logisticsNodeId,
            ShipmentLine shipmentLine
    ) {
        long remaining = shipmentLine.getQuantity();

        List<SupplyItemInventory> inventories = inventoryRepository.findReservedForUpdateByNode(
                supplier.getId(),
                item.getId(),
                logisticsNodeId,
                LocalDate.now()
        );

        for (SupplyItemInventory inventory : inventories) {
            if (remaining <= 0) {
                break;
            }

            long deductQty = Math.min(inventory.getReservedQty(), remaining);
            inventory.deductReserved(deductQty);
            allocationRepository.save(
                    ShipmentInventoryAllocation.deducted(
                            shipmentLine.getId(),
                            inventory.getInventoryId(),
                            deductQty
                    )
            );
            remaining -= deductQty;
        }

        if (remaining > 0) {
            throw new ItemInventoryException(ItemInventoryErrorCode.INVENTORY_INSUFFICIENT);
        }

        itemInventoryService.syncAvailableQtyForShipment(supplier, item);
    }
}
