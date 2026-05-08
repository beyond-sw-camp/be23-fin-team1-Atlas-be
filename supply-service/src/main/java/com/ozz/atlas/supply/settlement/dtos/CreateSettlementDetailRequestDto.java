package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Create Settlement Detail 값 요청")
public class CreateSettlementDetailRequestDto {

    @NotNull
    @Schema(description = "식별자", example = "1")
    private Long poItemId;

    @NotNull
    @Schema(description = "식별자", example = "1")
    private Long itemId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "수량", example = "1")
    private BigDecimal qty;

    @NotNull
    @DecimalMin(value = "0.00")
    @Schema(description = "가격", example = "1")
    private BigDecimal unitPrice;
}
