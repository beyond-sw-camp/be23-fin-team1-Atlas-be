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
@Table(name = "supply_purchase_order_item")
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
    private Long orderedQty;

    @Column(precision = 18, scale = 2)
    private Long confirmedQty;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lineAmount;

    @Column(nullable = false)
    private LocalDate requiredDate; // 요청 납기일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseOrderItemStatus itemStatus = PurchaseOrderItemStatus.OPEN;

    public static SupplyPurchaseOrderItem create(
            SupplyItem item,
            Long orderedQty,
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

    public Long getSubOrderBaseQty() {
        return this.confirmedQty != null ? this.confirmedQty : this.orderedQty;
    }

    public boolean isSubOrderable() {
        return this.itemStatus == PurchaseOrderItemStatus.OPEN
                || this.itemStatus == PurchaseOrderItemStatus.PARTIALLY_CONFIRMED
                || this.itemStatus == PurchaseOrderItemStatus.CONFIRMED;
    }

    public void update(
            SupplyItem item,
            Long orderedQty,
            BigDecimal unitPrice,
            LocalDate requiredDate
    ) {
        this.item = item;
        this.orderedQty = orderedQty;
        this.unitPrice = unitPrice;
        this.lineAmount = calculateLineAmount(orderedQty, unitPrice);
        this.requiredDate = requiredDate;
        this.confirmedQty = null;
        this.itemStatus = PurchaseOrderItemStatus.OPEN;
    }

    public void confirm(Long confirmedQty) {
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

    private static BigDecimal calculateLineAmount(Long orderedQty, BigDecimal unitPrice) {
        return BigDecimal.valueOf(orderedQty)
                .multiply(unitPrice)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
