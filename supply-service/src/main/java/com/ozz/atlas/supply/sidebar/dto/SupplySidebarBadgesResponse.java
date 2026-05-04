package com.ozz.atlas.supply.sidebar.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplySidebarBadgesResponse {
    private long ordersDesk;
    private long supplierControl;
    private long items;
    private long inventory;
    private long logisticsNodes;
    private long shipments;
    private long settlements;
    private long returns;
    private long certificateWatch;
}
