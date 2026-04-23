package com.ozz.atlas.supply.supplier.capability.dtos;

import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import jakarta.validation.constraints.*;
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
    @Positive
    private Long monthlyCapacity;

    @NotNull
    @Positive
    private Long availableQty;

    @NotNull
    @Positive
    private Long moq;

    private SupplierItemQualityGrade qualityGrade;
    private BigDecimal unitPriceHint;
    private LocalDate validFrom;

    @NotNull
    private Boolean partialConfirmationAllowed;

}
