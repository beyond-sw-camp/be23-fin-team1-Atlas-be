package com.ozz.atlas.supply.purchaseorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "supply_purchase_order_history")
public class PurchaseOrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column(nullable = false, length = 26)
    private String purchaseOrderPublicId;

    @Column(nullable = false, length = 50)
    private String poNumber;

    @Column(nullable = false, length = 26)
    private String buyerOrganizationPublicId;

    @Column(length = 26)
    private String supplierPublicId;

    @Column(length = 26)
    private String supplierOrganizationPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PurchaseOrderHistoryActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PoStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PoStatus afterStatus;

    @Column(length = 26)
    private String poItemPublicId;

    @Column(length = 26)
    private String itemPublicId;

    @Column(length = 120)
    private String itemName;

    private Long beforeOrderedQty;

    private Long afterOrderedQty;

    private Long beforeConfirmedQty;

    private Long afterConfirmedQty;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(length = 26)
    private String recordedBy;

    @PrePersist
    void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
