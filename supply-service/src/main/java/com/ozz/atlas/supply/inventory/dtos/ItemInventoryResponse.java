package com.ozz.atlas.supply.inventory.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import com.ozz.atlas.supply.item.domain.ItemUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item Inventory 값 응답")
public class ItemInventoryResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String inventoryPublicId;
    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String itemCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String itemName;
    @Schema(description = "unit 값", example = "sample", nullable = true)
    private ItemUnit unit;
    @Schema(description = "날짜", example = "2026-05-08", nullable = true)
    private LocalDate manufacturedDate;
    @Schema(description = "날짜", example = "2026-05-08", nullable = true)
    private LocalDate expirationDate;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long initialQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long remainingQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long reservedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long defectiveQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long availableQty;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private InventoryStatus status;
    @Schema(description = "메모", example = "샘플 내용", nullable = true)
    private String memo;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id", nullable = true)
    private String logisticsNodePublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String logisticsNodeCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String logisticsNodeName;
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
                .defectiveQty(inventory.getDefectiveQty())
                .availableQty(inventory.getAvailableQty())
                .status(inventory.getStatus())
                .memo(inventory.getMemo())
                .logisticsNodePublicId(inventory.getLogisticsNode().getPublicId())
                .logisticsNodeCode(inventory.getLogisticsNode().getNodeCode())
                .logisticsNodeName(inventory.getLogisticsNode().getNodeName())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
