package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Create Purchase Order Item 값 요청")
public class CreatePurchaseOrderItemRequest {

    @NotBlank
    @Size(max = 26)
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long orderedQty;

    @NotBlank(message = "도착거점은 필수입니다.")
    @Size(max = 26)
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String arrivalLogisticsNodePublicId;
}
