package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Sub Purchase Order Batch Line 값 요청")
public class CreateSubPurchaseOrderBatchLineRequest {

    @NotBlank
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id")
    private String supplierPublicId;

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String parentPoItemPublicId;

    @NotBlank
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull(message = "서브발주 수량은 필수입니다.")
    @Positive(message = "서브발주 수량은 0보다 커야 합니다.")
    @Schema(description = "수량", example = "1")
    private Long orderedQty;
}
