package com.ozz.atlas.supply.settlement.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateSettlementDetailRequestDto {

    @NotNull
    private Long poItemId;

    @NotNull
    private Long itemId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal qty;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal unitPrice;
}
