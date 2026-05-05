package com.ozz.atlas.supply.purchaseorder.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
public class SupplyPurchaseOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, unique = true, length = 50)
    private String poNumber;

    @Column(nullable = false, length = 26)
    private String buyerOrganizationPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private SupplySupplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PoStatus poStatus = PoStatus.CREATED;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CurrencyCode currencyCode = CurrencyCode.KRW;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(length = 26)
    private String createdByUserPublicId;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SupplyPurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

    public static SupplyPurchaseOrder create(
            String poNumber,
            String buyerOrganizationPublicId,
            SupplySupplier supplier,
            CurrencyCode currencyCode,
            String memo,
            String createdByUserPublicId,
            List<SupplyPurchaseOrderItem> purchaseOrderItems
    ) {
        SupplyPurchaseOrder purchaseOrder = SupplyPurchaseOrder.builder()
                .poNumber(poNumber)
                .buyerOrganizationPublicId(buyerOrganizationPublicId)
                .supplier(supplier)
                .orderedAt(LocalDateTime.now())
                .currencyCode(currencyCode != null ? currencyCode : CurrencyCode.KRW)
                .memo(memo)
                .createdByUserPublicId(createdByUserPublicId)
                .poStatus(PoStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (SupplyPurchaseOrderItem purchaseOrderItem : purchaseOrderItems) {
            purchaseOrder.addItem(purchaseOrderItem);
        }

        purchaseOrder.recalculateTotalAmount();
        return purchaseOrder;
    }

    public boolean isSubOrderCreatable() {
        return this.poStatus == PoStatus.PARTIALLY_CONFIRMED
                || this.poStatus == PoStatus.CONFIRMED;
    }

    public void updateHeader(
            String memo
    ) {
        if (memo != null) {
            this.memo = memo;
        }
    }

    public void addItem(SupplyPurchaseOrderItem purchaseOrderItem) {
        purchaseOrderItem.assignPurchaseOrder(this);
        this.purchaseOrderItems.add(purchaseOrderItem);
        recalculateTotalAmount();
    }

    public void reject() {
        this.poStatus = PoStatus.REJECTED;
        for (SupplyPurchaseOrderItem purchaseOrderItem : getActiveItems()) {
            purchaseOrderItem.markRejected();
        }
    }

    public void cancel() {
        this.poStatus = PoStatus.CANCELLED;
        for (SupplyPurchaseOrderItem purchaseOrderItem : getActiveItems()) {
            purchaseOrderItem.cancel();
        }
    }

    public void complete() {
        this.poStatus = PoStatus.COMPLETED;
    }

    public void delete() {
        this.poStatus = PoStatus.DELETED;
        for (SupplyPurchaseOrderItem purchaseOrderItem : getActiveItems()) {
            purchaseOrderItem.delete();
        }
    }

    public void refreshAfterItemChanged() {
        recalculateTotalAmount();
    }

    public void refreshConfirmationStatus() {
        List<SupplyPurchaseOrderItem> activeItems = getActiveItems();

        boolean allConfirmed = activeItems.stream()
                .allMatch(item -> item.getItemStatus() == PurchaseOrderItemStatus.CONFIRMED);

        boolean anyConfirmed = activeItems.stream()
                .anyMatch(item ->
                        item.getItemStatus() == PurchaseOrderItemStatus.PARTIALLY_CONFIRMED
                                || item.getItemStatus() == PurchaseOrderItemStatus.CONFIRMED
                );

        if (allConfirmed) {
            this.poStatus = PoStatus.CONFIRMED;
            return;
        }

        if (anyConfirmed) {
            this.poStatus = PoStatus.PARTIALLY_CONFIRMED;
            return;
        }

        this.poStatus = PoStatus.CREATED;
    }

    public List<SupplyPurchaseOrderItem> getActiveItems() {
        return this.purchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .toList();
    }

    public SupplyPurchaseOrderItem findActiveItemByPublicId(String poItemPublicId) {
        return getActiveItems().stream()
                .filter(item -> item.getPublicId().equals(poItemPublicId))
                .findFirst()
                .orElse(null);
    }

    private void recalculateTotalAmount() {
        this.totalAmount = this.purchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .map(item -> item.getLineAmount() == null ? BigDecimal.ZERO : item.getLineAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
