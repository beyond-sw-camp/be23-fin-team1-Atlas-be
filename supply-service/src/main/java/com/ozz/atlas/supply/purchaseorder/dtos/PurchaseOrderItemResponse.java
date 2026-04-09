package com.ozz.atlas.supply.purchaseorder.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse { // 발주 개별 아이템 정보용

    private String poItemPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal orderedQty;
    private BigDecimal confirmedQty;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private LocalDate requiredDate;
    private PurchaseOrderItemStatus itemStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PurchaseOrderItemResponse fromEntity(SupplyPurchaseOrderItem purchaseOrderItem) {
        return PurchaseOrderItemResponse.builder()
                .poItemPublicId(purchaseOrderItem.getPublicId())
                .itemPublicId(purchaseOrderItem.getItem().getPublicId())
                .itemCode(purchaseOrderItem.getItem().getItemCode())
                .itemName(purchaseOrderItem.getItem().getItemName())
                .unit(purchaseOrderItem.getItem().getUnit().name())
                .orderedQty(purchaseOrderItem.getOrderedQty())
                .confirmedQty(purchaseOrderItem.getConfirmedQty())
                .unitPrice(purchaseOrderItem.getUnitPrice())
                .lineAmount(purchaseOrderItem.getLineAmount())
                .requiredDate(purchaseOrderItem.getRequiredDate())
                .itemStatus(purchaseOrderItem.getItemStatus())
                .createdAt(purchaseOrderItem.getCreatedAt())
                .updatedAt(purchaseOrderItem.getUpdatedAt())
                .build();
    }
}
