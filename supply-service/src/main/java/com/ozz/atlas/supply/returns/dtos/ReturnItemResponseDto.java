package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ReturnItemResponseDto {
    private Long id;
    private String itemPublicId;
    private String lotPublicId;
    private BigDecimal returnQty;
    private String unit;
    private String detailReason;
    private String itemStatus;
    private List<String> attachmentPublicIds;

    public static ReturnItemResponseDto from(ReturnItem entity) {
        List<String> attachments = (entity.getAttachmentPublicIds() != null && !entity.getAttachmentPublicIds().isBlank())
                ? Arrays.asList(entity.getAttachmentPublicIds().split(","))
                : Collections.emptyList();

        return ReturnItemResponseDto.builder()
                .id(entity.getId())
                .itemPublicId(entity.getItemPublicId())
                .lotPublicId(entity.getLotPublicId())
                .returnQty(entity.getReturnQty())
                .unit(entity.getUnit())
                .detailReason(entity.getDetailReason())
                .itemStatus(entity.getItemStatus())
                .attachmentPublicIds(attachments)
                .build();
    }
}