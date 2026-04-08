package com.ozz.atlas.supply.supplier.esg.dtos;

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
public class CreateSupplyEsgAssessmentRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal environmentScore;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal socialScore;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal governanceScore;

    @Size(max = 50)
    private String evaluatorName;

    @Size(max = 1000)
    private String note;
}
