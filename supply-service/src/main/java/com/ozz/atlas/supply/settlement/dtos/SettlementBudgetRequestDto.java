package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Settlement Budget 값 요청")
public class SettlementBudgetRequestDto {

    // 예산 기준 연도
    @NotNull
    @Schema(description = "year 값", example = "1")
    private Integer year;

    // 예산 기준 월
    @NotNull
    @Min(1)
    @Max(12)
    @Schema(description = "month 값", example = "1")
    private Integer month;

    // 월 예산 금액
    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "금액", example = "1")
    private BigDecimal budgetAmount;

    // 예산 통화
    @NotNull
    @Schema(description = "코드", example = "CODE-001")
    private SettlementCurrency currencyCode;

    // 경고 기준 비율
    // 값이 없으면 80
    @DecimalMin("0.00")
    @Schema(description = "warning Threshold Rate 값", example = "1", nullable = true)
    private BigDecimal warningThresholdRate;
}
