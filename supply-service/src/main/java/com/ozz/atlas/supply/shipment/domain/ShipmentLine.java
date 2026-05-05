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
public class ShipmentLine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false)
    private Long shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentSourceType sourceType;

    @Column(nullable = false, length = 26)
    private String sourcePublicId;

    @Column(nullable = false, length = 26)
    private String sourceItemPublicId;

    @Column(nullable = false, length = 26)
    private String itemPublicId;

    @Column(nullable = false, length = 50)
    private String itemCode;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long originNodeId;

    @Column(nullable = false)
    private Long destinationNodeId;
}
