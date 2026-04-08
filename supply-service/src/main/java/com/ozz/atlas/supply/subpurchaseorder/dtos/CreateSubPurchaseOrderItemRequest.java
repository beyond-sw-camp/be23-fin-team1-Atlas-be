package com.ozz.atlas.supply.subpurchaseorder.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreateSubPurchaseOrderItemRequest {

    @NotBlank
    private String parentPoItemPublicId;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal orderedQty;

    private LocalDate requiredDate;
}
