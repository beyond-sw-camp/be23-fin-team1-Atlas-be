package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Purchase Order Batch Line 값 요청")
public class CreatePurchaseOrderBatchLineRequest {

    @NotBlank
    @Size(max = 26)
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id")
    private String supplierPublicId;

    @NotBlank
    @Size(max = 26)
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long orderedQty;

    // 도착거점 publicId
    @NotBlank(message = "도착거점은 필수입니다.")
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String arrivalLogisticsNodePublicId;
}
