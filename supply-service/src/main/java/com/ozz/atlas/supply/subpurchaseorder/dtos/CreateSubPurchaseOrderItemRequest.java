package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Create Sub Purchase Order Item 값 요청")
public class CreateSubPurchaseOrderItemRequest {

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String parentPoItemPublicId;

    @NotBlank
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long orderedQty;

}
