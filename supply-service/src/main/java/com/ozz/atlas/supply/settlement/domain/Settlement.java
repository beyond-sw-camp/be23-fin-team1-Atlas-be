package com.ozz.atlas.supply.settlement.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementTargetType targetType;

    @Column(nullable = false, length = 26)
    private String targetPublicId;

    private LocalDate settlementPeriodStart;
    private LocalDate settlementPeriodEnd;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementCurrency currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

//    정산 승인자 저장
    private LocalDateTime settledAt;

    @Column(length = 26)
    private String approvedByUserPublicId;

//    정산 취소자 저장
    private LocalDateTime cancelledAt;

    @Column(length = 26)
    private String cancelledByUserPublicId;


    //    정산 계산 결과가 확정되면 금액 갱신
    public void updateAmount(BigDecimal amount) {
        if (amount != null) {
            this.amount = amount;
        }
    }

//    대기 상태의 정산 승인 처리(정산 상태, 완료 시각, 승인자 기록)
    public void approve(String approvedByUserPublicId) {
        if (this.settlementStatus != SettlementStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 정산만 승인할 수 있습니다.");
        }

        this.settlementStatus = SettlementStatus.APPROVED;
        this.settledAt = LocalDateTime.now();
        this.approvedByUserPublicId = approvedByUserPublicId;
    }

//    대기 상태의 정산 취소 처리
    public void cancel(String cancelledByUserPublicId) {
        if (this.settlementStatus != SettlementStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 정산만 취소할 수 있습니다.");
        }

        this.settlementStatus = SettlementStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledByUserPublicId = cancelledByUserPublicId;
    }
}
