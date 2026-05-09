package com.ozz.atlas.supply.purchaseorder.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderHistory;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderHistoryActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PurchaseOrderHistoryResponse {

    private Long id;
    private String purchaseOrderPublicId;
    private String poNumber;
    private PurchaseOrderHistoryActionType actionType;
    private String actionLabel;
    private PoStatus beforeStatus;
    private PoStatus afterStatus;
    private String poItemPublicId;
    private String itemPublicId;
    private String itemName;
    private Long beforeOrderedQty;
    private Long afterOrderedQty;
    private Long beforeConfirmedQty;
    private Long afterConfirmedQty;
    private String memo;
    private LocalDateTime recordedAt;
    private String processedByUserPublicId;

    public static PurchaseOrderHistoryResponse from(PurchaseOrderHistory history) {
        return PurchaseOrderHistoryResponse.builder()
                .id(history.getId())
                .purchaseOrderPublicId(history.getPurchaseOrderPublicId())
                .poNumber(history.getPoNumber())
                .actionType(history.getActionType())
                .actionLabel(history.getActionType().getLabel())
                .beforeStatus(history.getBeforeStatus())
                .afterStatus(history.getAfterStatus())
                .poItemPublicId(history.getPoItemPublicId())
                .itemPublicId(history.getItemPublicId())
                .itemName(history.getItemName())
                .beforeOrderedQty(history.getBeforeOrderedQty())
                .afterOrderedQty(history.getAfterOrderedQty())
                .beforeConfirmedQty(history.getBeforeConfirmedQty())
                .afterConfirmedQty(history.getAfterConfirmedQty())
                .memo(history.getMemo())
                .recordedAt(history.getRecordedAt())
                .processedByUserPublicId(history.getRecordedBy())
                .build();
    }
}
