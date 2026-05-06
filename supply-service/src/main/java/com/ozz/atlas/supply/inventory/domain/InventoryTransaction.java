package com.ozz.atlas.supply.inventory.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory_transaction")
public class InventoryTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionReason reason;

    @Column(nullable = false)
    private Long quantityChange;

    @Column(length = 26)
    private String referenceId; // Return Public ID, Shipment Public ID 등

    @Column(nullable = false)
    private LocalDateTime transactionAt;

    @Builder
    public InventoryTransaction(Long inventoryId, String itemPublicId, TransactionReason reason, Long quantityChange, String referenceId) {
        this.inventoryId = inventoryId;
        this.itemPublicId = itemPublicId;
        this.reason = reason;
        this.quantityChange = quantityChange;
        this.referenceId = referenceId;
        this.transactionAt = LocalDateTime.now();
    }

    public enum TransactionReason {
        EXCHANGE_RESERVE,
        EXCHANGE_DEDUCT,
        RETURN_RESTOCK,
        RETURN_DEFECTIVE,
        ADJUSTMENT_OUT_DISPOSAL,
        INITIAL_STOCK,
        ORDER_DEDUCT
    }
}
