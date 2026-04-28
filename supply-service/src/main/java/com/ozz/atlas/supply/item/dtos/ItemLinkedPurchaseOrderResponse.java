package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemLinkedPurchaseOrderResponse {
    private String poPublicId;
    private String poNumber;
    private String buyerOrganizationPublicId;
    private PoStatus poStatus;
    private LocalDateTime orderedAt;
    private String poItemPublicId;
    private Long orderedQty;
    private Long confirmedQty;
    private PurchaseOrderItemStatus itemStatus;
    private LocalDate expectedDueDate;
}
