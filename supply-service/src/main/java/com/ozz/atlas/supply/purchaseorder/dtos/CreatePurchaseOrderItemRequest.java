package com.ozz.atlas.supply.purchaseorder.dtos;

import jakarta.validation.constraints.*;
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
public class CreatePurchaseOrderItemRequest {

    @NotBlank
    @Size(max = 26)
    private String itemPublicId;

    @NotNull
    @Positive
    private Long orderedQty;
}
