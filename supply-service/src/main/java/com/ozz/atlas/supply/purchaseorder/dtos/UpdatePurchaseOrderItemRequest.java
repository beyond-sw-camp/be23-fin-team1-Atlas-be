package com.ozz.atlas.supply.purchaseorder.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private Long orderedQty;
    private String arrivalLogisticsNodePublicId;
}
