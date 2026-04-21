package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.supplier.domain.SupplierTierLevel;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierListResponse {

    private String supplierCode;
    private String supplierName;
    private SupplierTierLevel tierLevel;
    private BigDecimal onTimeRate;
    private BigDecimal supplierScore;
    private BigDecimal qualityScore;
    private Long purchaseOrderCount;
    private BigDecimal totalAmount;
    private BigDecimal cumulativeAmount;
    private String status;
    private SupplierResponse detail;

    public static SupplierListResponse of(
            SupplySupplier supplier,
            BigDecimal onTimeRate,
            BigDecimal supplierScore,
            BigDecimal qualityScore,
            Long purchaseOrderCount,
            BigDecimal totalAmount,
            BigDecimal cumulativeAmount,
            String status
    ) {
        return SupplierListResponse.builder()
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .tierLevel(supplier.getTierLevel())
                .onTimeRate(onTimeRate)
                .supplierScore(supplierScore)
                .qualityScore(qualityScore)
                .purchaseOrderCount(purchaseOrderCount)
                .totalAmount(totalAmount)
                .cumulativeAmount(cumulativeAmount)
                .status(status)
                .detail(SupplierResponse.fromEntity(supplier))
                .build();
    }

}
