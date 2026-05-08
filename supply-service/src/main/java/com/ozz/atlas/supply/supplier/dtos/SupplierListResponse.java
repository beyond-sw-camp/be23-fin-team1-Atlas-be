package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Supplier List 값 응답")
public class SupplierListResponse {

    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "on Time Rate 값", example = "2026-05-08T10:00:00", nullable = true)
    private BigDecimal onTimeRate;
    @Schema(description = "supplier Score 값", example = "1", nullable = true)
    private BigDecimal supplierScore;
    @Schema(description = "quality Score 값", example = "1", nullable = true)
    private BigDecimal qualityScore;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long purchaseOrderCount;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal totalAmount;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal cumulativeAmount;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierRelationStatus relationStatus;
    @Schema(description = "detail 값", example = "sample", nullable = true)
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
