package com.ozz.atlas.supply.inventory.dtos;

import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.item.domain.ItemUnit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ItemInventoryResponse {

    private String inventoryPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private ItemUnit unit;
    private LocalDate manufacturedDate;
    private LocalDate expirationDate;
    private Long initialQty;
    private Long remainingQty;
    private Long reservedQty;
    private Long availableQty;
    private InventoryStatus status;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemInventoryResponse from(SupplyItemInventory inventory) {
        return ItemInventoryResponse.builder()
                .inventoryPublicId(inventory.getPublicId())
                .itemPublicId(inventory.getItem().getPublicId())
                .itemCode(inventory.getItem().getItemCode())
                .itemName(inventory.getItem().getItemName())
                .unit(inventory.getItem().getUnit())
                .manufacturedDate(inventory.getManufacturedDate())
                .expirationDate(inventory.getExpirationDate())
                .initialQty(inventory.getInitialQty())
                .remainingQty(inventory.getRemainingQty())
                .reservedQty(inventory.getReservedQty())
                .availableQty(inventory.getAvailableQty())
                .status(inventory.getStatus())
                .memo(inventory.getMemo())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
