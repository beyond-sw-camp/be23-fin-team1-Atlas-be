package com.ozz.atlas.supply.purchaseorder.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class SupplyPurchaseOrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyPurchaseOrder purchaseOrder;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
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

}
