package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItemHistory;
import com.ozz.atlas.supply.item.domain.SupplyItemHistoryActionType;
import com.ozz.atlas.supply.item.domain.SupplyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ItemHistoryResponse {

    private Long id;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private SupplyItemHistoryActionType actionType;
    private String actionLabel;
    private SupplyType beforeSupplyType;
    private SupplyType afterSupplyType;
    private Status beforeStatus;
    private Status afterStatus;
    private String beforePrimaryMediaFilePublicId;
    private String afterPrimaryMediaFilePublicId;
    private String memo;
    private LocalDateTime recordedAt;
    private String processedByUserPublicId;

    public static ItemHistoryResponse from(SupplyItemHistory history) {
        return ItemHistoryResponse.builder()
                .id(history.getId())
                .itemPublicId(history.getItemPublicId())
                .itemCode(history.getItemCode())
                .itemName(history.getItemName())
                .actionType(history.getActionType())
                .actionLabel(history.getActionType().getLabel())
                .beforeSupplyType(history.getBeforeSupplyType())
                .afterSupplyType(history.getAfterSupplyType())
                .beforeStatus(history.getBeforeStatus())
                .afterStatus(history.getAfterStatus())
                .beforePrimaryMediaFilePublicId(history.getBeforePrimaryMediaFilePublicId())
                .afterPrimaryMediaFilePublicId(history.getAfterPrimaryMediaFilePublicId())
                .memo(history.getMemo())
                .recordedAt(history.getRecordedAt())
                .processedByUserPublicId(history.getRecordedBy())
                .build();
    }
}
