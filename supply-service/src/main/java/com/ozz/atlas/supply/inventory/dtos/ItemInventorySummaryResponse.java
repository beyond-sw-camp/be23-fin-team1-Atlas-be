package com.ozz.atlas.supply.inventory.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Item Inventory Summary 값 응답")
public class ItemInventorySummaryResponse {
    @Schema(description = "수량", example = "1", nullable = true)
    private Long remainingQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long reservedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long availableQty;
    public static ItemInventorySummaryResponse of(Long remainingQty, Long reservedQty, Long availableQty) {
        return ItemInventorySummaryResponse.builder()
                .remainingQty(remainingQty != null ? remainingQty : 0L)
                .reservedQty(reservedQty != null ? reservedQty : 0L)
                .availableQty(availableQty != null ? availableQty : 0L)
                .build();
    }
}
