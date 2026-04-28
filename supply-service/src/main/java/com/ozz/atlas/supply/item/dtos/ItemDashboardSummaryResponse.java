package com.ozz.atlas.supply.item.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDashboardSummaryResponse {
    private long totalItemCount;
    private long activeItemCount;
    private long deactiveItemCount;
    private long todayOrderedItemCount;
}
