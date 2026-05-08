package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.BudgetUsageStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "Settlement Budget Usage 값 모델")
public class SettlementBudgetUsageDto {

    @Schema(description = "month 값", example = "1", nullable = true)
    private Integer month;

    @Schema(description = "label 값", example = "sample", nullable = true)
    private String label;

    // 해당 월 예산
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal budgetAmount;

    // 해당 월에 내가 지급해야 하는 정산액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal payableAmount;

    // 예산에서 정산액을 뺀 남은 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal remainingAmount;

    // 예산 사용률
    @Schema(description = "usage Rate 값", example = "1", nullable = true)
    private BigDecimal usageRate;

    // 예산 사용 상태
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private BudgetUsageStatus status;
}
