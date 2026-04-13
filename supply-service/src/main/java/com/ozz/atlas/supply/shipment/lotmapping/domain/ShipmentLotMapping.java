package com.ozz.atlas.supply.shipment.lotmapping.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class ShipmentLotMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Lot lot;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal shippedQty;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private LocalDateTime loadedAt;

    public ShipmentLotMapping(
            Shipment shipment,
            Lot lot,
            BigDecimal shippedQty,
            String unit,
            LocalDateTime loadedAt
    ) {
        this.shipment = shipment;
        this.lot = lot;
        this.shippedQty = shippedQty;
        this.unit = unit;
        this.loadedAt = loadedAt;
    }
}
