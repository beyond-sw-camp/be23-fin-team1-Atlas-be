package com.ozz.atlas.supply.returns.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "반품 품목 요청")
public class CreateReturnItemDto {
    @NotBlank(message = "품목 ID는 필수입니다.")
    @Schema(description = "품목 공개 식별자", example = "item_01HZY2ITEM123456789")
    private String itemPublicId;

    @Positive(message = "반품 수량은 0보다 커야 합니다.")
    @Schema(description = "반품 수량", example = "120.5")
    private BigDecimal returnQty;

    @NotBlank(message = "단위는 필수입니다.")
    @Schema(description = "수량 단위", example = "BOX")
    private String unit;

    @Schema(description = "품목별 상세 반품 사유", example = "포장 파손 및 냉장 온도 이탈")
    private String detailReason;
    
    @Schema(description = "품목 수준 첨부 파일 공개 식별자 목록", example = "[\"att_01HZY2ATT01\", \"att_01HZY2ATT02\"]")
    private List<String> attachmentPublicIds;
}
