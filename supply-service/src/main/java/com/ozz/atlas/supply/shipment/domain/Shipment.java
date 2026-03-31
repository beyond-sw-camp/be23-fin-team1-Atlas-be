package com.ozz.atlas.supply.shipment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 26)
    private String publicId;

    @Column(nullable = false, length = 50)
    private String shipmentNumber;

    private Long poId;
    private Long subPoId;

    @Column(length = 100)
    private String carrierName;

    @Column(length = 50)
    private String vehicleNo;

    @Column(length = 100)
    private String trackingNo;

    private String originNodeId;
    private String destinationNodeId;
    private String currentNodeId;
    private LocalDateTime departureEta;
    private LocalDateTime arrivalEta;
    private LocalDateTime actualDepartedAt;
    private LocalDateTime actualArrivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(nullable = false)
    private boolean temperatureRequired;
}
