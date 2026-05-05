package com.ozz.atlas.supply.subpurchaseorder.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "supply_sub_purchase_order")
public class SupplySubPurchaseOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_po_id")
    private Long subPoId;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(name = "sub_po_number", nullable = false, unique = true, length = 50)
    private String subPoNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_po_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplyPurchaseOrder parentPurchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_po_status", nullable = false)
    @Builder.Default
    private SubPoStatus subPoStatus = SubPoStatus.CREATED;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal totalAmount;

    @Column(name = "created_by_user_public_id", length = 26)
    private String createdByUserPublicId;

    @OneToMany(mappedBy = "subPurchaseOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SupplySubPurchaseOrderItem> subPurchaseOrderItems = new ArrayList<>();

    public static SupplySubPurchaseOrder create(
            String subPoNumber,
            SupplyPurchaseOrder parentPurchaseOrder,
            SupplySupplier supplier,
            String createdByUserPublicId,
            List<SupplySubPurchaseOrderItem> items
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = SupplySubPurchaseOrder.builder()
                .subPoNumber(subPoNumber)
                .parentPurchaseOrder(parentPurchaseOrder)
                .supplier(supplier)
                .subPoStatus(SubPoStatus.CREATED)
                .orderedAt(LocalDateTime.now())
                .totalAmount(java.math.BigDecimal.ZERO)
                .createdByUserPublicId(createdByUserPublicId)
                .build();

        for (SupplySubPurchaseOrderItem item : items) {
            subPurchaseOrder.addItem(item);
        }

        return subPurchaseOrder;
    }

    public void addItem(SupplySubPurchaseOrderItem item) {
        item.assignSubPurchaseOrder(this);
        this.subPurchaseOrderItems.add(item);
        recalculateTotalAmount();
    }

    public void reject() {
        this.subPoStatus = SubPoStatus.REJECTED;
        for (SupplySubPurchaseOrderItem item : getActiveItems()) {
            item.markRejected();
        }
    }

    public List<SupplySubPurchaseOrderItem> getActiveItems() {
        return this.subPurchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .toList();
    }

    public SupplySubPurchaseOrderItem findActiveItem(String parentPoItemPublicId, String itemPublicId) {
        return getActiveItems().stream()
                .filter(item ->
                        item.getParentPurchaseOrderItem().getPublicId().equals(parentPoItemPublicId)
                                && item.getItem().getPublicId().equals(itemPublicId)
                )
                .findFirst()
                .orElse(null);
    }

    public void refreshConfirmationStatus() {
        List<SupplySubPurchaseOrderItem> activeItems = getActiveItems();

        boolean allConfirmed = activeItems.stream()
                .allMatch(item -> item.getLineStatus() == SubPurchaseOrderLineStatus.CONFIRMED);

        boolean anyConfirmed = activeItems.stream()
                .anyMatch(item ->
                        item.getLineStatus() == SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED
                                || item.getLineStatus() == SubPurchaseOrderLineStatus.CONFIRMED
                );

        if (allConfirmed) {
            this.subPoStatus = SubPoStatus.CONFIRMED;
            return;
        }

        if (anyConfirmed) {
            this.subPoStatus = SubPoStatus.PARTIALLY_CONFIRMED;
            return;
        }

        this.subPoStatus = SubPoStatus.CREATED;
    }

    private void recalculateTotalAmount() {
        this.totalAmount = this.subPurchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .map(SupplySubPurchaseOrderItem::getLineAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void refreshAfterItemChanged() {
        recalculateTotalAmount();
    }
}
