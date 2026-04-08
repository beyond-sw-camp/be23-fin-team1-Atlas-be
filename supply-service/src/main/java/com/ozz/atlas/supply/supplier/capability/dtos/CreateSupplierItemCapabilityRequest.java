package com.ozz.atlas.supply.supplier.capability.dtos;

import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateSupplierItemCapabilityRequest {

    @NotBlank
    private String itemPublicId;

    @NotNull
    @Min(0)
    private Integer leadTimeDays;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyCapacity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal availableQty;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal moq;

    private SupplierItemQualityGrade qualityGrade;
    private BigDecimal unitPriceHint;
    private LocalDate validFrom;
}
