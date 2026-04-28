package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
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

    public enum OrderType {
        PURCHASE_ORDER,
        SUB_PURCHASE_ORDER
    }

    public enum OrderRole {
        ISSUED,
        RECEIVED
    }

    private OrderType orderType;
    private String poPublicId;
    private String poNumber;
    private String subPoPublicId;
    private String subPoNumber;
    private String parentPoNumber;
    private OrderRole orderRole;
    private String status;
    private LocalDateTime orderedAt;
    private BigDecimal totalAmount;

    public static ConnectedSupplierOrderResponse fromPurchaseOrder(SupplyPurchaseOrder purchaseOrder) {
        return ConnectedSupplierOrderResponse.builder()
                .orderType(OrderType.PURCHASE_ORDER)
                .poPublicId(purchaseOrder.getPublicId())
                .poNumber(purchaseOrder.getPoNumber())
                .orderRole(OrderRole.RECEIVED)
                .status(purchaseOrder.getPoStatus().name())
                .orderedAt(purchaseOrder.getOrderedAt())
                .totalAmount(purchaseOrder.getTotalAmount())
                .build();
    }

    public static ConnectedSupplierOrderResponse fromSubPurchaseOrder(
            Long loginSupplierId,
            SupplySubPurchaseOrder subPurchaseOrder
    ) {
        boolean issuedByLoginSupplier =
                subPurchaseOrder.getParentPurchaseOrder().getSupplier().getId().equals(loginSupplierId);

        return ConnectedSupplierOrderResponse.builder()
                .orderType(OrderType.SUB_PURCHASE_ORDER)
                .subPoPublicId(subPurchaseOrder.getPublicId())
                .subPoNumber(subPurchaseOrder.getSubPoNumber())
                .parentPoNumber(subPurchaseOrder.getParentPurchaseOrder().getPoNumber())
                .orderRole(issuedByLoginSupplier ? OrderRole.ISSUED : OrderRole.RECEIVED)
                .status(subPurchaseOrder.getSubPoStatus().name())
                .orderedAt(subPurchaseOrder.getOrderedAt())
                .totalAmount(subPurchaseOrder.getTotalAmount())
                .build();
    }
}
