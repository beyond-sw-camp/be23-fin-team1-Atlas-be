package com.ozz.atlas.supply.purchaseorder.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
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
public class PurchaseOrderItemResponse { // 발주 개별 아이템 정보용

    private String poItemPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private String unit;
    private Long orderedQty;
    private Long confirmedQty;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private PurchaseOrderItemStatus itemStatus;
    private LocalDate expectedDueDate;
    private Integer leadTimeDays;
    private Boolean partialConfirmationAllowed;
    private String arrivalLogisticsNodePublicId;
    private String arrivalLogisticsNodeName;
    private String arrivalLogisticsNodeAddress;
    private String arrivalLogisticsNodeCode;
    private Long alreadyShippedQty;
    private Long remainingShippableQty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PurchaseOrderItemResponse fromEntity(SupplyPurchaseOrderItem purchaseOrderItem) {
        LogisticsNode arrivalLogisticsNode = purchaseOrderItem.getArrivalLogisticsNode();
        return PurchaseOrderItemResponse.builder()
                .poItemPublicId(purchaseOrderItem.getPublicId())
                .itemPublicId(purchaseOrderItem.getItem().getPublicId())
                .itemCode(purchaseOrderItem.getItem().getItemCode())
                .itemName(purchaseOrderItem.getItem().getItemName())
                .unit(purchaseOrderItem.getItem().getUnit().name())
                .orderedQty(purchaseOrderItem.getOrderedQty())
                .confirmedQty(purchaseOrderItem.getConfirmedQty())
                .unitPrice(purchaseOrderItem.getUnitPrice())
                .lineAmount(purchaseOrderItem.getLineAmount())
                .itemStatus(purchaseOrderItem.getItemStatus())
                .expectedDueDate(purchaseOrderItem.getExpectedDueDate())
                .leadTimeDays(purchaseOrderItem.getLeadTimeDays())
                .partialConfirmationAllowed(purchaseOrderItem.getPartialConfirmationAllowed())
                .createdAt(purchaseOrderItem.getCreatedAt())
                .updatedAt(purchaseOrderItem.getUpdatedAt())
                .alreadyShippedQty(0L)
                .remainingShippableQty(purchaseOrderItem.getConfirmedQty())
                .arrivalLogisticsNodePublicId(arrivalLogisticsNode != null ? arrivalLogisticsNode.getPublicId() : null)
                .arrivalLogisticsNodeName(arrivalLogisticsNode  != null ? arrivalLogisticsNode.getNodeName() : null)
                .arrivalLogisticsNodeAddress(arrivalLogisticsNode  != null ? arrivalLogisticsNode.getAddress() : null)
                .arrivalLogisticsNodeCode(arrivalLogisticsNode != null ? arrivalLogisticsNode.getNodeCode() : null)
                .build();
    }
}
