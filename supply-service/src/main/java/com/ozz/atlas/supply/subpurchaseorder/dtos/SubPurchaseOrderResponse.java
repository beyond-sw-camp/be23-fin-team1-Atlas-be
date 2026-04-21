package com.ozz.atlas.supply.subpurchaseorder.dtos;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPurchaseOrderResponse {

    private String subPoPublicId;
    private String subPoNumber;
    private String parentPoPublicId;
    private String parentPoNumber;
    private String issuerSupplierPublicId;
    private String issuerSupplierName;
    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal totalAmount;
    private SubPoStatus subPoStatus;
    private LocalDateTime orderedAt;
    private LocalDate dueDate;
    private String createdByUserPublicId;
    private List<SubPurchaseOrderItemResponse> items;

    public static SubPurchaseOrderResponse fromEntity(SupplySubPurchaseOrder subPo, boolean includeItems) {
        return SubPurchaseOrderResponse.builder()
                .subPoPublicId(subPo.getPublicId())
                .subPoNumber(subPo.getSubPoNumber())
                .parentPoPublicId(subPo.getParentPurchaseOrder().getPublicId())
                .parentPoNumber(subPo.getParentPurchaseOrder().getPoNumber())
                .issuerSupplierPublicId(subPo.getParentPurchaseOrder().getSupplier().getPublicId())
                .issuerSupplierName(subPo.getParentPurchaseOrder().getSupplier().getSupplierName())
                .supplierPublicId(subPo.getSupplier().getPublicId())
                .supplierCode(subPo.getSupplier().getSupplierCode())
                .supplierName(subPo.getSupplier().getSupplierName())
                .totalAmount(subPo.getTotalAmount())
                .subPoStatus(subPo.getSubPoStatus())
                .orderedAt(subPo.getOrderedAt())
                .dueDate(subPo.getDueDate())
                .createdByUserPublicId(subPo.getCreatedByUserPublicId())
                .items(
                        includeItems
                                ? subPo.getActiveItems().stream()
                                .map(SubPurchaseOrderItemResponse::fromEntity)
                                .toList()
                                : null
                )
                .build();
    }
}
