package com.ozz.atlas.supply.purchaseorder.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePurchaseOrderItemRequest {

    private String itemPublicId;
    private BigDecimal orderedQty;
    private BigDecimal unitPrice;
    private LocalDate requiredDate;
}
