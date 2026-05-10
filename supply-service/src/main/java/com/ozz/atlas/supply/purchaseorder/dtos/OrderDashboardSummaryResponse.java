package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    @Schema(description = "추가 발주 필요 건수", example = "1")
    private Long shortageOrderCount;
    @Schema(description = "추가 발주 필요 발주 목록")
    private List<OrderShortageResponse> shortageOrders;

    public static OrderDashboardSummaryResponse of(
            Long totalOrderCount,
            Long pendingOrderCount,
            Long completedOrderCount,
            Long issuedOrderCount,
            Long receivedOrderCount,
            BigDecimal totalAmount,
            Long shortageOrderCount,
            List<OrderShortageResponse> shortageOrders
    ) {
        List<OrderShortageResponse> safeShortageOrders = shortageOrders != null ? shortageOrders : List.of();
        return OrderDashboardSummaryResponse.builder()
                .totalOrderCount(totalOrderCount != null ? totalOrderCount : 0L)
                .pendingOrderCount(pendingOrderCount != null ? pendingOrderCount : 0L)
                .completedOrderCount(completedOrderCount != null ? completedOrderCount : 0L)
                .issuedOrderCount(issuedOrderCount != null ? issuedOrderCount : 0L)
                .receivedOrderCount(receivedOrderCount != null ? receivedOrderCount : 0L)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .shortageOrderCount(shortageOrderCount != null ? shortageOrderCount : 0L)
                .shortageOrders(safeShortageOrders)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "추가 발주 필요 발주 요약")
    public static class OrderShortageResponse {
        @Schema(description = "발주 공개 식별자", example = "01HQPO123456789ABCDEF")
        private String poPublicId;
        @Schema(description = "발주 번호", example = "PO-2026-0000001")
        private String poNumber;
        @Schema(description = "대표 발주 품목 공개 식별자", example = "01HQPOITEM123456789")
        private String poItemPublicId;
        @Schema(description = "협력사 공개 식별자", example = "01HQSUPPLIER123456789")
        private String supplierPublicId;
        @Schema(description = "협력사명", example = "메인 협력사")
        private String supplierName;
        @Schema(description = "부족 품목 대표명", example = "토마토 슬라이스")
        private String itemName;
        @Schema(description = "부족 품목 대표 공개 식별자", example = "01HQITEM123456789")
        private String itemPublicId;
        @Schema(description = "부족 품목 수", example = "2")
        private Long shortageItemCount;
        @Schema(description = "대표 부족 수량", example = "100")
        private Long shortageQty;
        @Schema(description = "대표 단위", example = "EA")
        private String unit;
    }
}
