package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
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
    private BigDecimal onTimeRate;
    private BigDecimal supplierScore;
    private BigDecimal qualityScore;
    private Long purchaseOrderCount;
    private BigDecimal totalAmount;
    private BigDecimal cumulativeAmount;
    private SupplierRelationStatus relationStatus;
    private SupplierResponse detail;

    public static SupplierListResponse of(
            SupplySupplier supplier,
            BigDecimal onTimeRate,
            BigDecimal supplierScore,
            BigDecimal qualityScore,
            Long purchaseOrderCount,
            BigDecimal totalAmount,
            BigDecimal cumulativeAmount,
            SupplierRelationStatus relationStatus
    ) {
        return SupplierListResponse.builder()
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .onTimeRate(onTimeRate)
                .supplierScore(supplierScore)
                .qualityScore(qualityScore)
                .purchaseOrderCount(purchaseOrderCount)
                .totalAmount(totalAmount)
                .cumulativeAmount(cumulativeAmount)
                .relationStatus(relationStatus)
                .detail(SupplierResponse.fromEntity(supplier))
                .build();
    }
}
