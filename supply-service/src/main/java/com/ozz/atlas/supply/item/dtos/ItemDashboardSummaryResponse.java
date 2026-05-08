package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item Dashboard Summary 값 응답")
public class ItemDashboardSummaryResponse {
    @Schema(description = "개수", example = "1", nullable = true)
    private long totalItemCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long activeItemCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long deactiveItemCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private long todayOrderedItemCount;
}
