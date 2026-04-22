package com.ozz.atlas.supply.subpurchaseorder.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSubPurchaseOrderItemRequest {

    @NotNull
    @Positive
    private Long confirmedQty;
}
