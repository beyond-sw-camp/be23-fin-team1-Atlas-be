package com.ozz.atlas.supply.supplier.relation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "날짜 요청")
public class UpdateSupplierRelationRequest {

    @Min(1)
    @Schema(description = "priority Rank 값", example = "1", nullable = true)
    private Integer priorityRank;

    @Schema(description = "effective From 값", example = "sample", nullable = true)
    private LocalDate effectiveFrom;
    @Schema(description = "effective To 값", example = "sample", nullable = true)
    private LocalDate effectiveTo;
}
