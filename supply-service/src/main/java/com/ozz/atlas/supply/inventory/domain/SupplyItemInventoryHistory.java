package com.ozz.atlas.supply.inventory.domain;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "supply_item_inventory_history")
public class SupplyItemInventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false, length = 26)
    private String inventoryPublicId;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @Column(nullable = false, length = 50)
    private String itemCode;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SupplyItemInventoryHistoryActionType actionType;

    @Column
    private Long quantityChange;

    @Column
    private Long beforeInitialQty;

    @Column
    private Long afterInitialQty;

    @Column
    private Long beforeRemainingQty;

    @Column
    private Long afterRemainingQty;

    @Column
    private Long beforeReservedQty;

    @Column
    private Long afterReservedQty;

    @Column
    private Long beforeDefectiveQty;

    @Column
    private Long afterDefectiveQty;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InventoryStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InventoryStatus afterStatus;

    @Column
    private LocalDate beforeManufacturedDate;

    @Column
    private LocalDate afterManufacturedDate;

    @Column
    private LocalDate beforeExpirationDate;

    @Column
    private LocalDate afterExpirationDate;

    @Column(length = 26)
    private String referenceId;

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
