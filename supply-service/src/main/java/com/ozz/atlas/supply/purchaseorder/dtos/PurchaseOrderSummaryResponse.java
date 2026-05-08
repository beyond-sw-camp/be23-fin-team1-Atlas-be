package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.purchaseorder.domain.CurrencyCode;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
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
@Schema(description = "Purchase Order Summary 값 응답")
public class PurchaseOrderSummaryResponse { // 발주 목록 조회용

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String poPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String poNumber;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String buyerOrganizationPublicId;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private PoStatus poStatus;
    @Schema(description = "ordered At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime orderedAt;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal totalAmount;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private CurrencyCode currencyCode;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierStatus supplierStatus;
    public static PurchaseOrderSummaryResponse fromEntity(SupplyPurchaseOrder purchaseOrder) {
        return PurchaseOrderSummaryResponse.builder()
                .poPublicId(purchaseOrder.getPublicId())
                .poNumber(purchaseOrder.getPoNumber())
                .buyerOrganizationPublicId(purchaseOrder.getBuyerOrganizationPublicId())
                .supplierPublicId(purchaseOrder.getSupplier().getPublicId())
                .supplierCode(purchaseOrder.getSupplier().getSupplierCode())
                .supplierName(purchaseOrder.getSupplier().getSupplierName())
                .poStatus(purchaseOrder.getPoStatus())
                .orderedAt(purchaseOrder.getOrderedAt())
                .totalAmount(purchaseOrder.getTotalAmount())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .supplierStatus(purchaseOrder.getSupplier().getSupplierStatus())
                .build();
    }
}
