package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@Schema(description = "반품 품목 응답")
public class ReturnItemResponseDto {
    @Schema(description = "반품 품목 내부 ID", example = "1")
    private Long id;
    @Schema(description = "품목 공개 식별자", example = "item_01HZY2ITEM123456789")
    private String itemPublicId;
    @Schema(description = "품목명", example = "냉동 생지")
    private String itemName;
    @Schema(description = "반품 수량", example = "120.5")
    private BigDecimal returnQty;
    @Schema(description = "수량 단위", example = "BOX")
    private String unit;
    @Schema(description = "품목별 상세 사유", example = "포장 파손 및 냉장 온도 이탈")
    private String detailReason;
    @Schema(description = "품목 상태", example = "PENDING")
    private String itemStatus;
    @Schema(description = "QC 검수 상태", example = "PASS")
    private String qcStatus;
    @Schema(description = "QC 검수 등급", example = "A")
    private String qcGrade;
    @Schema(description = "폐기 사유", example = "EXPIRED")
    private String disposalReason;
    @Schema(description = "폐기 증빙 첨부 파일 식별자", example = "att_01HZY2ATT02")
    private String disposalProofAttachmentPublicId;
    @Schema(description = "품목 첨부 파일 공개 식별자 목록", example = "[\"att_01HZY2ATT01\"]")
    private List<String> attachmentPublicIds;
    public static ReturnItemResponseDto from(ReturnItem entity, String itemName) {
        List<String> attachments = (entity.getAttachmentPublicIds() != null && !entity.getAttachmentPublicIds().isBlank())
                ? Arrays.asList(entity.getAttachmentPublicIds().split(","))
                : Collections.emptyList();

        return ReturnItemResponseDto.builder()
                .id(entity.getId())
                .itemPublicId(entity.getItemPublicId())
                .itemName(itemName)
                .returnQty(entity.getReturnQty())
                .unit(entity.getUnit())
                .detailReason(entity.getDetailReason())
                .itemStatus(entity.getItemStatus())
                .qcStatus(entity.getQcStatus())
                .qcGrade(entity.getQcGrade())
                .disposalReason(entity.getDisposalReason())
                .disposalProofAttachmentPublicId(entity.getDisposalProofAttachmentPublicId())
                .attachmentPublicIds(attachments)
                .build();
    }
}
