package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.SettlementDetailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Schema(description = "Settlement Detail 값 응답")
public class SettlementDetailResponseDto {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long poItemId;
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long itemId;
    @Schema(description = "수량", example = "1", nullable = true)
    private BigDecimal qty;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal amount;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SettlementDetailStatus detailStatus;
}
