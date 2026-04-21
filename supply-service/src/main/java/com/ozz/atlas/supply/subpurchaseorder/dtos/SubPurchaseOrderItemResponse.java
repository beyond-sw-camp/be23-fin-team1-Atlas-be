package com.ozz.atlas.supply.subpurchaseorder.dtos;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
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
public class SubPurchaseOrderItemResponse {

    private String parentPoItemPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private BigDecimal orderedQty;
    private BigDecimal confirmedQty;
    private LocalDate requiredDate;
    private SubPurchaseOrderLineStatus lineStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubPurchaseOrderItemResponse fromEntity(SupplySubPurchaseOrderItem item) {
        return SubPurchaseOrderItemResponse.builder()
                .parentPoItemPublicId(item.getParentPurchaseOrderItem().getPublicId())
                .itemPublicId(item.getItem().getPublicId())
                .itemCode(item.getItem().getItemCode())
                .itemName(item.getItem().getItemName())
                .unit(item.getItem().getUnit().name())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .orderedQty(item.getOrderedQty())
                .confirmedQty(item.getConfirmedQty())
                .requiredDate(item.getRequiredDate())
                .lineStatus(item.getLineStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
