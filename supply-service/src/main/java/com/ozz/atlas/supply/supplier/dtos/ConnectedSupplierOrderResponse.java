package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedSupplierOrderResponse {

    public enum OrderRole {
        ISSUED,
        RECEIVED
    }

    private String subPoPublicId;
    private String subPoNumber;
    private String parentPoNumber;
    private OrderRole orderRole;
    private SubPoStatus subPoStatus;
    private LocalDateTime orderedAt;
    private BigDecimal totalAmount;

    public static ConnectedSupplierOrderResponse of(
            Long loginSupplierId,
            SupplySubPurchaseOrder subPurchaseOrder
    ) {
        boolean issuedByLoginSupplier =
                subPurchaseOrder.getParentPurchaseOrder().getSupplier().getId().equals(loginSupplierId);

        return ConnectedSupplierOrderResponse.builder()
                .subPoPublicId(subPurchaseOrder.getPublicId())
                .subPoNumber(subPurchaseOrder.getSubPoNumber())
                .parentPoNumber(subPurchaseOrder.getParentPurchaseOrder().getPoNumber())
                .orderRole(issuedByLoginSupplier ? OrderRole.ISSUED : OrderRole.RECEIVED)
                .subPoStatus(subPurchaseOrder.getSubPoStatus())
                .orderedAt(subPurchaseOrder.getOrderedAt())
                .totalAmount(subPurchaseOrder.getTotalAmount())
                .build();
    }
}
