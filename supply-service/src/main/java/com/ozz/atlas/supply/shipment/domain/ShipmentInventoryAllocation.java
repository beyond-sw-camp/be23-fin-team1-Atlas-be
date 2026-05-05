package com.ozz.atlas.supply.shipment.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class ShipmentInventoryAllocation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false)
    private Long shipmentLineId;

    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private Long reservedQty;

    @Column(nullable = false)
    @Builder.Default
    private Long deductedQty = 0L;

    public static ShipmentInventoryAllocation create(
            Long shipmentLineId,
            Long inventoryId,
            Long reservedQty
    ) {
        return ShipmentInventoryAllocation.builder()
                .shipmentLineId(shipmentLineId)
                .inventoryId(inventoryId)
                .reservedQty(reservedQty)
                .deductedQty(0L)
                .build();
    }

    public static ShipmentInventoryAllocation deducted(
            Long shipmentLineId,
            Long inventoryId,
            Long deductedQty
    ) {
        return ShipmentInventoryAllocation.builder()
                .shipmentLineId(shipmentLineId)
                .inventoryId(inventoryId)
                .reservedQty(deductedQty)
                .deductedQty(deductedQty)
                .build();
    }

    public Long getRemainingDeductQty() {
        return reservedQty - deductedQty;
    }

    public void markDeducted(Long qty) {
        if (qty == null || qty <= 0 || deductedQty + qty > reservedQty) {
            throw new IllegalArgumentException("차감 가능한 출하 예약 수량이 부족합니다.");
        }

        this.deductedQty += qty;
    }
}
