package com.ozz.atlas.supply.inventory.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemInventorySummaryResponse {
    private Long remainingQty;
    private Long reservedQty;
    private Long availableQty;

    public static ItemInventorySummaryResponse of(Long remainingQty, Long reservedQty, Long availableQty) {
        return ItemInventorySummaryResponse.builder()
                .remainingQty(remainingQty != null ? remainingQty : 0L)
                .reservedQty(reservedQty != null ? reservedQty : 0L)
                .availableQty(availableQty != null ? availableQty : 0L)
                .build();
    }
}
