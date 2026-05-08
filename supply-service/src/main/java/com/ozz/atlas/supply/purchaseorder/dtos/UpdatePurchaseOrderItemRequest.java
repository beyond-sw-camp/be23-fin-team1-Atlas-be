package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "날짜 요청")
public class UpdatePurchaseOrderItemRequest {

    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;
    @Positive
    @Schema(description = "수량", example = "1", nullable = true)
    private Long orderedQty;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String arrivalLogisticsNodePublicId;
}
