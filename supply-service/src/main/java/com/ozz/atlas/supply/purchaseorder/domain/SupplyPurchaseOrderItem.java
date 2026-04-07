package com.ozz.atlas.supply.purchaseorder.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
public class SupplyPurchaseOrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poItemId;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyPurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItem item;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal orderedQty;

    @Column(precision = 18, scale = 2)
    private BigDecimal confirmedQty;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lineAmount;

    @Column(nullable = false)
    private LocalDate requiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseOrderItemStatus itemStatus = PurchaseOrderItemStatus.OPEN;

    public static SupplyPurchaseOrderItem create(
            SupplyItem item,
            BigDecimal orderedQty,
            BigDecimal unitPrice,
            LocalDate requiredDate
    ) {
        return SupplyPurchaseOrderItem.builder()
                .item(item)
                .orderedQty(orderedQty)
                .unitPrice(unitPrice)
                .lineAmount(calculateLineAmount(orderedQty, unitPrice))
                .requiredDate(requiredDate)
                .itemStatus(PurchaseOrderItemStatus.OPEN)
                .build();
    }

    // 발주 상세는 수정 시에도 lineAmount를 다시 계산해야 총액이 맞는다.
    public void update(
            SupplyItem item,
            BigDecimal orderedQty,
            BigDecimal unitPrice,
            LocalDate requiredDate
    ) {
        this.item = item;
        this.orderedQty = orderedQty;
        this.unitPrice = unitPrice;
        this.lineAmount = calculateLineAmount(orderedQty, unitPrice);
        this.requiredDate = requiredDate;

        // 수정이 들어가면 다시 확정 전 상태로 되돌린다.
        this.confirmedQty = null;
        this.itemStatus = PurchaseOrderItemStatus.OPEN;
    }

    // 협력사가 실제 공급 가능한 수량을 입력하면 상세 상태를 계산한다.
    public void confirm(BigDecimal confirmedQty) {
        this.confirmedQty = confirmedQty;

        if (confirmedQty.compareTo(this.orderedQty) < 0) {
            this.itemStatus = PurchaseOrderItemStatus.PARTIALLY_CONFIRMED;
            return;
        }

        this.itemStatus = PurchaseOrderItemStatus.CONFIRMED;
    }

    public void markRejected() {
        this.itemStatus = PurchaseOrderItemStatus.REJECTED;
    }

    public void cancel() {
        this.itemStatus = PurchaseOrderItemStatus.CANCELLED;
    }

    public void delete() {
        this.itemStatus = PurchaseOrderItemStatus.DELETED;
    }

    public boolean isDeleted() {
        return this.itemStatus == PurchaseOrderItemStatus.DELETED;
    }

    void assignPurchaseOrder(SupplyPurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    private static BigDecimal calculateLineAmount(BigDecimal orderedQty, BigDecimal unitPrice) {
        return orderedQty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }
}
