package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Sub Purchase Order 값 요청")
public class CreateSubPurchaseOrderRequest {

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String parentPoPublicId;

    @NotBlank
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id")
    private String supplierPublicId;

    @Valid
    @NotEmpty
    @Schema(description = "항목 목록")
    private List<CreateSubPurchaseOrderItemRequest> items;
}
