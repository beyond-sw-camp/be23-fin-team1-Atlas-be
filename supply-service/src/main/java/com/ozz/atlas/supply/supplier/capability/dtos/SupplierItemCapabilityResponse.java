package com.ozz.atlas.supply.supplier.capability.dtos;

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
public class SupplierItemCapabilityResponse {

    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private Integer leadTimeDays;
    private Long monthlyCapacity;
    private Long availableQty;
    private Long moq;
    private SupplierItemQualityGrade qualityGrade;
    private Boolean partialConfirmationAllowed;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal unitPriceHint;
    private LocalDate validFrom;
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
