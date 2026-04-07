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

    @Column(nullable = false, length = 50)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PriorityCode priorityCode = PriorityCode.NORMAL;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

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
            PriorityCode priorityCode,
            LocalDate dueDate,
            CurrencyCode currencyCode,
            String memo,
            String createdByUserPublicId,
            List<SupplyPurchaseOrderItem> purchaseOrderItems
    ) {
        SupplyPurchaseOrder purchaseOrder = SupplyPurchaseOrder.builder()
                .poNumber(poNumber)
                .buyerOrganizationPublicId(buyerOrganizationPublicId)
                .supplier(supplier)
                .priorityCode(priorityCode != null ? priorityCode : PriorityCode.NORMAL)
                .orderedAt(LocalDateTime.now())
                .dueDate(dueDate)
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

    // 발주 헤더 수정은 발주번호/우선순위/납기일/메모만 바꾼다.
    public void updateHeader(
            String poNumber,
            PriorityCode priorityCode,
            LocalDate dueDate,
            String memo
    ) {
        if (poNumber != null && !poNumber.isBlank()) {
            this.poNumber = poNumber;
        }
        if (priorityCode != null) {
            this.priorityCode = priorityCode;
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }

    // 발주 상세 추가 시 연관관계를 같이 세팅하고 총액을 다시 계산한다.
    public void addItem(SupplyPurchaseOrderItem purchaseOrderItem) {
        purchaseOrderItem.assignPurchaseOrder(this);
        this.purchaseOrderItems.add(purchaseOrderItem);
        recalculateTotalAmount();
    }

    public void accept() {
        this.poStatus = PoStatus.ACCEPTED;
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

    // 실제 row delete 대신 상태값으로 숨긴다.
    public void delete() {
        this.poStatus = PoStatus.DELETED;
        for (SupplyPurchaseOrderItem purchaseOrderItem : getActiveItems()) {
            purchaseOrderItem.delete();
        }
    }

    // 상세가 수정/삭제되면 총액을 다시 계산해야 헤더 금액이 맞다.
    public void refreshAfterItemChanged() {
        recalculateTotalAmount();
    }

    // 상세 확정수량 입력 후 헤더 상태를 자동 갱신한다.
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

        this.poStatus = PoStatus.ACCEPTED;
    }

    public List<SupplyPurchaseOrderItem> getActiveItems() {
        return this.purchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .toList();
    }

    private void recalculateTotalAmount() {
        this.totalAmount = this.purchaseOrderItems.stream()
                .filter(item -> !item.isDeleted())
                .map(SupplyPurchaseOrderItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
