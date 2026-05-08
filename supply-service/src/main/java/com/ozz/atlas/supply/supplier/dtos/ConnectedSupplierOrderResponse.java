package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Connected Supplier Order 값 응답")
public class ConnectedSupplierOrderResponse {
    public enum OrderType {
        PURCHASE_ORDER,
        SUB_PURCHASE_ORDER
    }
    public enum OrderRole {
        ISSUED,
        RECEIVED
    }

    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private OrderType orderType;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String poPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String poNumber;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String subPoPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String subPoNumber;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String parentPoNumber;
    @Schema(description = "order Role 값", example = "sample", nullable = true)
    private OrderRole orderRole;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String status;
    @Schema(description = "ordered At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime orderedAt;
    @Schema(description = "금액", example = "1", nullable = true)
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
