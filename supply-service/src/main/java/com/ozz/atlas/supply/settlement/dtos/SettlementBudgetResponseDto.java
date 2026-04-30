package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.SettlementBudget;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SettlementBudgetResponseDto {

    private String publicId;
    private String organizationPublicId;
    private Integer year;
    private Integer month;
    private BigDecimal budgetAmount;
    private SettlementCurrency currencyCode;
    private BigDecimal warningThresholdRate;

    public static SettlementBudgetResponseDto fromEntity(SettlementBudget budget) {
        return SettlementBudgetResponseDto.builder()
                .publicId(budget.getPublicId())
                .organizationPublicId(budget.getOrganizationPublicId())
                .year(budget.getYear())
                .month(budget.getMonth())
                .budgetAmount(budget.getBudgetAmount())
                .currencyCode(budget.getCurrencyCode())
                .warningThresholdRate(budget.getWarningThresholdRate())
                .build();
    }
}
