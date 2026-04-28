package com.ozz.atlas.supply.purchaseorder.dtos;

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
public class CreatePurchaseOrderBatchLineRequest {

    @NotBlank
    @Size(max = 26)
    private String supplierPublicId;

    @NotBlank
    @Size(max = 26)
    private String itemPublicId;

    @NotNull
    @Positive
    private Long orderedQty;

    // 도착거점 publicId
    @NotBlank(message = "도착거점은 필수입니다.")
    private String arrivalLogisticsNodePublicId;
}
