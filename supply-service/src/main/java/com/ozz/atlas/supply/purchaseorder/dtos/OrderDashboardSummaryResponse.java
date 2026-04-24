package com.ozz.atlas.supply.purchaseorder.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDashboardSummaryResponse {

    private Long totalOrderCount;
    private Long pendingOrderCount;
    private Long completedOrderCount;
    private Long issuedOrderCount;
    private Long receivedOrderCount;
    private BigDecimal totalAmount;

    public static OrderDashboardSummaryResponse of(
            Long totalOrderCount,
            Long pendingOrderCount,
            Long completedOrderCount,
            Long issuedOrderCount,
            Long receivedOrderCount,
            BigDecimal totalAmount
    ) {
        return OrderDashboardSummaryResponse.builder()
                .totalOrderCount(totalOrderCount != null ? totalOrderCount : 0L)
                .pendingOrderCount(pendingOrderCount != null ? pendingOrderCount : 0L)
                .completedOrderCount(completedOrderCount != null ? completedOrderCount : 0L)
                .issuedOrderCount(issuedOrderCount != null ? issuedOrderCount : 0L)
                .receivedOrderCount(receivedOrderCount != null ? receivedOrderCount : 0L)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .build();
    }
}
