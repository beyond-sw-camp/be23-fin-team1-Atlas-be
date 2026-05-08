package com.ozz.atlas.supply.supplier.capability.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Create Supplier Item Capability 값 요청")
public class CreateSupplierItemCapabilityRequest {

    @NotBlank
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull
    @Min(0)
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00")
    private Integer leadTimeDays;

    @NotNull
    @Positive
    @Schema(description = "monthly Capacity 값", example = "1")
    private Long monthlyCapacity;

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long availableQty;

    @NotNull
    @Positive
    @Schema(description = "moq 값", example = "1")
    private Long moq;

    @Schema(description = "quality Grade 값", example = "sample", nullable = true)
    private SupplierItemQualityGrade qualityGrade;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPriceHint;
    @Schema(description = "식별자", example = "sample", nullable = true)
    private LocalDate validFrom;

    @NotNull
    @Schema(description = "partial Confirmation Allowed 값", example = "true")
    private Boolean partialConfirmationAllowed;

}
