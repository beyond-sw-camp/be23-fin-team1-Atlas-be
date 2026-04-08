package com.ozz.atlas.supply.supplier.capability.dtos;

import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class UpdateSupplierItemCapabilityRequest {

    @Min(0)
    private Integer leadTimeDays;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyCapacity;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal availableQty;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal moq;

    private SupplierItemQualityGrade qualityGrade;
    private BigDecimal unitPriceHint;
    private LocalDate validFrom;
}
