package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.SettlementBudget;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "Settlement Budget 값 응답")
public class SettlementBudgetResponseDto {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;
    @Schema(description = "year 값", example = "1", nullable = true)
    private Integer year;
    @Schema(description = "month 값", example = "1", nullable = true)
    private Integer month;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal budgetAmount;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private SettlementCurrency currencyCode;
    @Schema(description = "warning Threshold Rate 값", example = "1", nullable = true)
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
