package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order Dashboard Summary 값 응답")
public class OrderDashboardSummaryResponse {

    @Schema(description = "개수", example = "1", nullable = true)
    private Long totalOrderCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long pendingOrderCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long completedOrderCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long issuedOrderCount;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long receivedOrderCount;
    @Schema(description = "금액", example = "1", nullable = true)
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
