package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Confirm Purchase Order Item 값 요청")
public class ConfirmPurchaseOrderItemRequest {

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long confirmedQty;
}
