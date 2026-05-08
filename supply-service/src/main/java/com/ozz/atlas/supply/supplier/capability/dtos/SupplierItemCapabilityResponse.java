package com.ozz.atlas.supply.supplier.capability.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Supplier Item Capability 값 응답")
public class SupplierItemCapabilityResponse {

    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String itemCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String itemName;
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer leadTimeDays;
    @Schema(description = "monthly Capacity 값", example = "1", nullable = true)
    private Long monthlyCapacity;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long availableQty;
    @Schema(description = "moq 값", example = "1", nullable = true)
    private Long moq;
    @Schema(description = "quality Grade 값", example = "sample", nullable = true)
    private SupplierItemQualityGrade qualityGrade;
    @Schema(description = "partial Confirmation Allowed 값", example = "true", nullable = true)
    private Boolean partialConfirmationAllowed;
    @Schema(description = "unit 값", example = "sample", nullable = true)
    private String unit;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPriceHint;
    @Schema(description = "식별자", example = "sample", nullable = true)
    private LocalDate validFrom;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    public static SupplierItemCapabilityResponse fromEntity(SupplySupplierItemCapability capability) {
        return SupplierItemCapabilityResponse.builder()
                .supplierPublicId(capability.getSupplier().getPublicId())
                .supplierCode(capability.getSupplier().getSupplierCode())
                .supplierName(capability.getSupplier().getSupplierName())
                .itemPublicId(capability.getItem().getPublicId())
                .itemCode(capability.getItem().getItemCode())
                .itemName(capability.getItem().getItemName())
                .leadTimeDays(capability.getLeadTimeDays())
                .monthlyCapacity(capability.getMonthlyCapacity())
                .availableQty(capability.getAvailableQty())
                .moq(capability.getMoq())
                .qualityGrade(capability.getQualityGrade())
                .unitPriceHint(capability.getUnitPriceHint())
                .validFrom(capability.getValidFrom())
                .unit(capability.getItem().getUnit().name())
                .unitPrice(capability.getItem().getUnitPrice())
                .partialConfirmationAllowed(capability.getPartialConfirmationAllowed())
                .createdAt(capability.getCreatedAt())
                .build();
    }
}
