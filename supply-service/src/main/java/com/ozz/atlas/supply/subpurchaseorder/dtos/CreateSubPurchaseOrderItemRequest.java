package com.ozz.atlas.supply.subpurchaseorder.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateSubPurchaseOrderItemRequest {

    @NotBlank
    private String parentPoItemPublicId;

    @NotBlank
    private String itemPublicId;

    @NotNull
    @Positive
    private Long orderedQty;

}
