package com.ozz.atlas.supply.inventory.dtos;

import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventoryHistory;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventoryHistoryActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ItemInventoryHistoryResponse {

    private Long id;
    private String inventoryPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private SupplyItemInventoryHistoryActionType actionType;
    private String actionLabel;
    private Long quantityChange;
    private Long beforeInitialQty;
    private Long afterInitialQty;
    private Long beforeRemainingQty;
    private Long afterRemainingQty;
    private Long beforeReservedQty;
    private Long afterReservedQty;
    private Long beforeDefectiveQty;
    private Long afterDefectiveQty;
    private InventoryStatus beforeStatus;
    private InventoryStatus afterStatus;
    private LocalDate beforeManufacturedDate;
    private LocalDate afterManufacturedDate;
    private LocalDate beforeExpirationDate;
    private LocalDate afterExpirationDate;
    private String referenceId;
    private String memo;
    private LocalDateTime recordedAt;
    private String processedByUserPublicId;

    public static ItemInventoryHistoryResponse from(SupplyItemInventoryHistory history) {
        return ItemInventoryHistoryResponse.builder()
                .id(history.getId())
                .inventoryPublicId(history.getInventoryPublicId())
                .itemPublicId(history.getItemPublicId())
                .itemCode(history.getItemCode())
                .itemName(history.getItemName())
                .actionType(history.getActionType())
                .actionLabel(history.getActionType().getLabel())
                .quantityChange(history.getQuantityChange())
                .beforeInitialQty(history.getBeforeInitialQty())
                .afterInitialQty(history.getAfterInitialQty())
                .beforeRemainingQty(history.getBeforeRemainingQty())
                .afterRemainingQty(history.getAfterRemainingQty())
                .beforeReservedQty(history.getBeforeReservedQty())
                .afterReservedQty(history.getAfterReservedQty())
                .beforeDefectiveQty(history.getBeforeDefectiveQty())
                .afterDefectiveQty(history.getAfterDefectiveQty())
                .beforeStatus(history.getBeforeStatus())
                .afterStatus(history.getAfterStatus())
                .beforeManufacturedDate(history.getBeforeManufacturedDate())
                .afterManufacturedDate(history.getAfterManufacturedDate())
                .beforeExpirationDate(history.getBeforeExpirationDate())
                .afterExpirationDate(history.getAfterExpirationDate())
                .referenceId(history.getReferenceId())
                .memo(history.getMemo())
                .recordedAt(history.getRecordedAt())
                .processedByUserPublicId(history.getRecordedBy())
                .build();
    }
}
