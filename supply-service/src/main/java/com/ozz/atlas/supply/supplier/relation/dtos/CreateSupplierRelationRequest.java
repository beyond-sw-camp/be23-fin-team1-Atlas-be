package com.ozz.atlas.supply.supplier.relation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "Create Supplier Relation 값 요청")
public class CreateSupplierRelationRequest {

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String childSupplierPublicId;

    @NotNull
    @Min(1)
    @Schema(description = "priority Rank 값", example = "1")
    private Integer priorityRank;

    @Schema(description = "effective From 값", example = "sample", nullable = true)
    private LocalDate effectiveFrom;
    @Schema(description = "effective To 값", example = "sample", nullable = true)
    private LocalDate effectiveTo;
}
