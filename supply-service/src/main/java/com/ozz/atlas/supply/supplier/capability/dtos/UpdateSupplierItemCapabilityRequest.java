package com.ozz.atlas.supply.supplier.capability.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "날짜 요청")
public class UpdateSupplierItemCapabilityRequest {

    @Min(0)
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer leadTimeDays;
    @Positive
    @Schema(description = "monthly Capacity 값", example = "1", nullable = true)
    private Long monthlyCapacity;

    @Positive
    @Schema(description = "수량", example = "1", nullable = true)
    private Long availableQty;

    @Positive
    @Schema(description = "moq 값", example = "1", nullable = true)
    private Long moq;

    @Schema(description = "quality Grade 값", example = "sample", nullable = true)
    private SupplierItemQualityGrade qualityGrade;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPriceHint;
    @Schema(description = "식별자", example = "sample", nullable = true)
    private LocalDate validFrom;
    @Schema(description = "partial Confirmation Allowed 값", example = "true", nullable = true)
    private Boolean partialConfirmationAllowed;

}
