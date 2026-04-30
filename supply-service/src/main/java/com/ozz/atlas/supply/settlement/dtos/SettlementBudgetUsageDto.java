package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.BudgetUsageStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SettlementBudgetUsageDto {

    private Integer month;

    private String label;

    // 해당 월 예산
    private BigDecimal budgetAmount;

    // 해당 월에 내가 지급해야 하는 정산액
    private BigDecimal payableAmount;

    // 예산에서 정산액을 뺀 남은 금액
    private BigDecimal remainingAmount;

    // 예산 사용률
    private BigDecimal usageRate;

    // 예산 사용 상태
    private BudgetUsageStatus status;
}
