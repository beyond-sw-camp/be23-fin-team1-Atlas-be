package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.BudgetUsageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// 정산 통계 화면에서 사용하는 전체 응답 DTO입니다.
// KPI 카드, 월별 차트, 상태별 차트, 예산 사용률 데이터를 한 번에 내려줍니다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Settlement Statistics 값 응답")
public class SettlementStatisticsResponseDto {

    @Schema(description = "year 값", example = "1", nullable = true)
    private Integer year;

    // 올해 전체 정산 금액
    // 내가 지급할 돈과 받을 돈을 모두 포함한 기존 통계
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal totalAmountThisYear;

    // 이번 달 전체 정산 금액
    // 내가 지급할 돈과 받을 돈을 모두 포함한 기존 통계
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal totalAmountThisMonth;

    // 대기 상태인 전체 정산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal pendingAmount;

    // 승인 완료된 전체 정산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal approvedAmount;

    // 취소된 전체 정산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal cancelledAmount;

    // 전체 정산 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long totalCount;

    // 대기 상태 정산 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long pendingCount;

    // 승인 완료 정산 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long approvedCount;

    // 취소 정산 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long cancelledCount;

    // 전체 정산 중 승인 완료 비율
    @Schema(description = "approval Rate 값", example = "1", nullable = true)
    private BigDecimal approvalRate;

    // 올해 내가 지급해야 하는 정산 금액입
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal payableAmountThisYear;

    // 이번 달 내가 지급해야 하는 정산 금액
    // 예산 사용률 계산 기준
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal payableAmountThisMonth;

    // 올해 내가 받을 정산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal receivableAmountThisYear;

    // 이번 달 내가 받을 정산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal receivableAmountThisMonth;

    // 이번 달 예산 금액
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal currentMonthBudgetAmount;

    // 이번 달 예산에서 지급 정산액을 뺀 잔여 예산
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal currentMonthRemainingBudgetAmount;

    // 이번 달 예산 사용률
    @Schema(description = "current Month Budget Usage Rate 값", example = "1", nullable = true)
    private BigDecimal currentMonthBudgetUsageRate;

    // 이번 달 예산 사용 상태
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private BudgetUsageStatus currentMonthBudgetStatus;

    // 연도별 전체 정산 금액 차트 데이터
    @Schema(description = "금액", example = "1", nullable = true)
    private List<ChartPointDto> yearlyAmounts;

    // 월별 전체 정산 금액 차트 데이터
    @Schema(description = "금액", example = "1", nullable = true)
    private List<ChartPointDto> monthlyAmounts;

    // 상태별 전체 정산 금액 차트 데이터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private List<ChartPointDto> statusAmounts;

    // 정산 대상 유형별 금액 차트 데이터
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private List<ChartPointDto> targetTypeAmounts;

    // 월별 예산 대비 지급 정산액 차트 데이터
    @Schema(description = "monthly Budget Usages 값", nullable = true)
    private List<SettlementBudgetUsageDto> monthlyBudgetUsages;

    // 발주 전체 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long purchaseOrderCount;

    // 아직 처리 중인 발주 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long pendingPurchaseOrderCount;

    // 현재 이동 중인 배송 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long inTransitShipmentCount;

    // 지연 상태인 배송 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long delayedShipmentCount;

    // 아직 완료되지 않은 반품 진행 건수
    @Schema(description = "개수", example = "1", nullable = true)
    private Long returnInProgressCount;

    // 운영 상태 차트용 데이터
// 예: 발주 전체, 발주 대기, 배송중, 배송지연, 반품진행
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private List<ChartPointDto> operationStatusCounts;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointDto {

        // 화면에 보여줄 이름
        // 예: 4월, 승인 완료, 출하
        @Schema(description = "label 값", example = "sample", nullable = true)
        private String label;

        // 실제 계산이나 식별에 사용하는 값
        @Schema(description = "value 값", example = "sample", nullable = true)
        private String value;

        // 해당 항목의 정산 금액
        @Schema(description = "금액", example = "1", nullable = true)
        private BigDecimal amount;

        // 해당 항목의 정산 건수
        @Schema(description = "개수", example = "1", nullable = true)
        private Long count;
    }
}
