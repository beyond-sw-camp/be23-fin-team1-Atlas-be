package com.ozz.atlas.supply.inventory.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "supply_item_inventory")
public class SupplyItemInventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplySupplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private SupplyItem item;

    @Column(nullable = false)
    private LocalDate manufacturedDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private Long initialQty;

    @Column(nullable = false)
    private Long remainingQty;

    @Column(nullable = false)
    private Long reservedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryStatus status;

    @Column(length = 500)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logistics_node_id", nullable = false)
    private LogisticsNode logisticsNode;

    public static SupplyItemInventory create(
            SupplySupplier supplier,
            SupplyItem item,
            LogisticsNode logisticsNode,
            LocalDate manufacturedDate,
            LocalDate expirationDate,
            Long qty,
            String memo
    ) {
        return SupplyItemInventory.builder()
                .supplier(supplier)
                .item(item)
                .logisticsNode(logisticsNode)
                .manufacturedDate(manufacturedDate)
                .expirationDate(expirationDate)
                .initialQty(qty)
                .remainingQty(qty)
                .reservedQty(0L)
                .status(InventoryStatus.ACTIVE)
                .memo(memo)
                .build();
    }

    public Long getAvailableQty() {
        return remainingQty - reservedQty;
    }

    public void reserve(Long qty) {
        if (qty == null || qty <= 0 || getAvailableQty() < qty) {
            throw new IllegalArgumentException("예약 가능한 재고 수량이 부족합니다.");
        }

        this.reservedQty += qty;
        refreshStatus();
    }

    public void deductReserved(Long qty) {
        if (qty == null || qty <= 0 || this.reservedQty < qty || this.remainingQty < qty) {
            throw new IllegalArgumentException("차감 가능한 예약 재고 수량이 부족합니다.");
        }

        this.reservedQty -= qty;
        this.remainingQty -= qty;
        refreshStatus();
    }

    public void update(LogisticsNode logisticsNode, LocalDate manufacturedDate, LocalDate expirationDate, Long qty, String memo) {
        if (this.status != InventoryStatus.ACTIVE || this.reservedQty > 0) {
            throw new IllegalStateException("예약된 재고는 수정할 수 없습니다.");
        }
        this.logisticsNode = logisticsNode;
        this.manufacturedDate = manufacturedDate;
        this.expirationDate = expirationDate;
        this.initialQty = qty;
        this.remainingQty = qty;
        this.memo = memo;
        refreshStatus();
    }

    public void delete() {
        if (this.reservedQty > 0) {
            throw new IllegalStateException("예약된 재고는 삭제할 수 없습니다.");
        }
        this.status = InventoryStatus.DELETED;
    }

    private void refreshStatus() {
        if (this.status == InventoryStatus.DELETED) {
            return;
        }

        if (this.remainingQty <= 0) {
            this.status = InventoryStatus.EXHAUSTED;
            return;
        }

        if (this.expirationDate != null && this.expirationDate.isBefore(LocalDate.now())) {
            this.status = InventoryStatus.EXPIRED;
            return;
        }

        if (this.reservedQty > 0) {
            this.status = InventoryStatus.RESERVED;
            return;
        }

        this.status = InventoryStatus.ACTIVE;
    }

    public void deductRemainingOnly(Long qty) {
        if (qty == null || qty <= 0 || this.remainingQty < qty) {
            throw new IllegalArgumentException("차감 가능한 재고 수량이 부족합니다.");
        }

        this.remainingQty -= qty;
        refreshStatus();
    }

}
