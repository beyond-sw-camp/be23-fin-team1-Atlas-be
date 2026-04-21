package com.ozz.atlas.supply.supplier.dtos;

import java.math.BigDecimal;

public record SupplierPurchaseOrderSummary(
        Long purchaseOrderCount, // 발주수
        BigDecimal totalAmount // 누적금액
) {
    public static SupplierPurchaseOrderSummary empty() {
        return new SupplierPurchaseOrderSummary(0L, BigDecimal.ZERO);
    }
}
