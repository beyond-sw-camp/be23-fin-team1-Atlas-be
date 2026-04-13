package com.ozz.atlas.supply.settlement.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
public class SettlementDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Settlement settlement;

    @Column(nullable = false)
    private Long poItemId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal qty;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementDetailStatus detailStatus = SettlementDetailStatus.PENDING;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    // 수량 또는 단가 변경 시 상세 금액을 다시 계산
    public void recalculateAmount(BigDecimal qty, BigDecimal unitPrice) {
        if (qty != null) {
            this.qty = qty;
        }
        if (unitPrice != null) {
            this.unitPrice = unitPrice;
        }
        this.amount = this.qty.multiply(this.unitPrice);
    }

    // 정산 상세 항목을 승인 상태로 전환
    public void approve() {
        if (this.detailStatus != SettlementDetailStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 정산 상세만 승인할 수 있습니다.");
        }
        this.detailStatus = SettlementDetailStatus.APPROVED;
    }

    // 정산 상세 항목을 취소 상태로 전환
    public void cancel() {
        if (this.detailStatus != SettlementDetailStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 정산 상세만 취소할 수 있습니다.");
        }
        this.detailStatus = SettlementDetailStatus.CANCELLED;
    }
}
