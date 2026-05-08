package com.ozz.atlas.supply.supplier.esg.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Supply Esg Assessment 값 요청")
public class CreateSupplyEsgAssessmentRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Schema(description = "environment Score 값", example = "1")
    private BigDecimal environmentScore;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Schema(description = "social Score 값", example = "1")
    private BigDecimal socialScore;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Schema(description = "governance Score 값", example = "1")
    private BigDecimal governanceScore;

    @Size(max = 50)
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String evaluatorName;

    @Size(max = 1000)
    @Schema(description = "note 값", example = "sample", nullable = true)
    private String note;
}
