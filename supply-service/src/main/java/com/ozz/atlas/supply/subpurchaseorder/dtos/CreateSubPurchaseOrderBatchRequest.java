package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Sub Purchase Order Batch 값 요청")
public class CreateSubPurchaseOrderBatchRequest {

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String parentPoPublicId;

    @Valid
    @NotEmpty
    @Schema(description = "lines 값")
    private List<CreateSubPurchaseOrderBatchLineRequest> lines;
}
