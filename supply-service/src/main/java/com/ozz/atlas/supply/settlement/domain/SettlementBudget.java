package com.ozz.atlas.supply.settlement.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "settlement_budget",
        uniqueConstraints = {
                // 한 조직은 같은 연도/월/통화에 예산을 하나만
                @UniqueConstraint(
                        name = "uk_settlement_budget_org_year_month_currency",
                        columnNames = {
                                "organization_public_id",
                                "budget_year",
                                "budget_month",
                                "currency_code"
                        }
                )
        }
)
public class SettlementBudget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부에 노출할 예산 publicId
    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    // 예산을 가진 조직 publicId
    @Column(name = "organization_public_id", nullable = false, length = 26)
    private String organizationPublicId;

    // 예산 기준 연도
    @Column(name = "budget_year", nullable = false)
    private Integer year;

    // 예산 기준 월
    @Column(name = "budget_month", nullable = false)
    private Integer month;

    // 월 예산 금액
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal budgetAmount;

    // 예산 통화
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false, length = 20)
    private SettlementCurrency currencyCode;

    // 경고 기준 비율
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal warningThresholdRate = BigDecimal.valueOf(80);

    // 예산을 마지막으로 저장한 사용자 publicId
    @Column(length = 26)
    private String updatedByUserPublicId;

    // 기존 월 예산을 수정할 때 사용
    public void update(
            BigDecimal budgetAmount,
            BigDecimal warningThresholdRate,
            String updatedByUserPublicId
    ) {
        // null이 들어오면 기존 값을 유지
        if (budgetAmount != null) {
            this.budgetAmount = budgetAmount;
        }

        // 경고 기준을 안 보내면 기존 값을 유지
        if (warningThresholdRate != null) {
            this.warningThresholdRate = warningThresholdRate;
        }

        this.updatedByUserPublicId = updatedByUserPublicId;
    }
}
