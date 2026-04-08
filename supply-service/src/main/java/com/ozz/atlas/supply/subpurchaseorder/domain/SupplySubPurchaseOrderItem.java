package com.ozz.atlas.supply.subpurchaseorder.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "supply_sub_purchase_order_item")
public class SupplySubPurchaseOrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_po_item_id")
    private Long subPoItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_po_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySubPurchaseOrder subPurchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyPurchaseOrderItem parentPurchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyItem item;

    @Column(name = "ordered_qty", nullable = false, precision = 18, scale = 2)
    private BigDecimal orderedQty;

    @Column(name = "confirmed_qty", precision = 18, scale = 2)
    private BigDecimal confirmedQty;

    @Column(name = "required_date", nullable = false)
    private LocalDate requiredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_status", nullable = false)
    @Builder.Default
    private SubPurchaseOrderLineStatus lineStatus = SubPurchaseOrderLineStatus.OPEN;

    // 하위발주 상세는 부모 발주 상세를 기준으로 생성되고,
    // item도 부모 상세의 item을 그대로 따라가야 데이터가 틀어지지 않는다.
    public static SupplySubPurchaseOrderItem create(
            SupplyPurchaseOrderItem parentPurchaseOrderItem,
            BigDecimal orderedQty,
            LocalDate requiredDate
    ) {
        return SupplySubPurchaseOrderItem.builder()
                .parentPurchaseOrderItem(parentPurchaseOrderItem)
                .item(parentPurchaseOrderItem.getItem())
                .orderedQty(orderedQty)
                .requiredDate(requiredDate)
                .lineStatus(SubPurchaseOrderLineStatus.OPEN)
                .build();
    }

    public void confirm(BigDecimal confirmedQty) {
        this.confirmedQty = confirmedQty;

        if (confirmedQty.compareTo(this.orderedQty) < 0) {
            this.lineStatus = SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED;
            return;
        }

        this.lineStatus = SubPurchaseOrderLineStatus.CONFIRMED;
    }

    public void markRejected() {
        this.lineStatus = SubPurchaseOrderLineStatus.REJECTED;
    }

    public boolean isDeleted() {
        return this.lineStatus == SubPurchaseOrderLineStatus.DELETED;
    }

    void assignSubPurchaseOrder(SupplySubPurchaseOrder subPurchaseOrder) {
        this.subPurchaseOrder = subPurchaseOrder;
    }
}
