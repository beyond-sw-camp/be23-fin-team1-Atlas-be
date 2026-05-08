package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Supplier Purchase Order Summary 값 모델")
public record SupplierPurchaseOrderSummary(
        Long purchaseOrderCount, // 발주수
        BigDecimal totalAmount // 누적금액
) {
    public static SupplierPurchaseOrderSummary empty() {
        return new SupplierPurchaseOrderSummary(0L, BigDecimal.ZERO);
    }
}
