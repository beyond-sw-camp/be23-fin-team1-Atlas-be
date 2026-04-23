package com.ozz.atlas.supply.supplier.capability.dtos;

import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private Long monthlyCapacity;

    @Positive
    private Long availableQty;

    @Positive
    private Long moq;

    private SupplierItemQualityGrade qualityGrade;
    private BigDecimal unitPriceHint;
    private LocalDate validFrom;
    private Boolean partialConfirmationAllowed;

}
