package com.ozz.atlas.supply.purchaseorder.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.CurrencyCode;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PriorityCode;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
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
public class PurchaseOrderSummaryResponse { // 발주 목록 조회용

    private String poPublicId;
    private String poNumber;
    private String buyerOrganizationPublicId;
    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private PoStatus poStatus;
    private PriorityCode priorityCode;
    private LocalDateTime orderedAt;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private CurrencyCode currencyCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PurchaseOrderSummaryResponse fromEntity(SupplyPurchaseOrder purchaseOrder) {
        return PurchaseOrderSummaryResponse.builder()
                .poPublicId(purchaseOrder.getPublicId())
                .poNumber(purchaseOrder.getPoNumber())
                .buyerOrganizationPublicId(purchaseOrder.getBuyerOrganizationPublicId())
                .supplierPublicId(purchaseOrder.getSupplier().getPublicId())
                .supplierCode(purchaseOrder.getSupplier().getSupplierCode())
                .supplierName(purchaseOrder.getSupplier().getSupplierName())
                .poStatus(purchaseOrder.getPoStatus())
                .priorityCode(purchaseOrder.getPriorityCode())
                .orderedAt(purchaseOrder.getOrderedAt())
                .dueDate(purchaseOrder.getDueDate())
                .totalAmount(purchaseOrder.getTotalAmount())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .build();
    }
}
