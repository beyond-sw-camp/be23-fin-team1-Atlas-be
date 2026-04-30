package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class SettlementBudgetRequestDto {

    // 예산 기준 연도
    @NotNull
    private Integer year;

    // 예산 기준 월
    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    // 월 예산 금액
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal budgetAmount;

    // 예산 통화
    @NotNull
    private SettlementCurrency currencyCode;

    // 경고 기준 비율
    // 값이 없으면 80
    @DecimalMin("0.00")
    private BigDecimal warningThresholdRate;
}
