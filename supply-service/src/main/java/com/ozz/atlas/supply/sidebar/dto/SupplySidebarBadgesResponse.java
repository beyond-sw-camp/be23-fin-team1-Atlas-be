package com.ozz.atlas.supply.sidebar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "식별자 응답")
public class SupplySidebarBadgesResponse {
    @Schema(description = "orders Desk 값", example = "1", nullable = true)
    private long ordersDesk;
    @Schema(description = "supplier Control 값", example = "1", nullable = true)
    private long supplierControl;
    @Schema(description = "항목 목록", example = "1", nullable = true)
    private long items;
    @Schema(description = "inventory 값", example = "1", nullable = true)
    private long inventory;
    @Schema(description = "logistics Nodes 값", example = "1", nullable = true)
    private long logisticsNodes;
    @Schema(description = "shipments 값", example = "1", nullable = true)
    private long shipments;
    @Schema(description = "settlements 값", example = "1", nullable = true)
    private long settlements;
    @Schema(description = "returns 값", example = "1", nullable = true)
    private long returns;
    @Schema(description = "certificate Watch 값", example = "1", nullable = true)
    private long certificateWatch;
}
