package com.ozz.atlas.supply.shipment.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
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

    @Column(nullable = false, unique = true, updatable = false, length = 26)
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

    private Long originNodeId;
    private Long destinationNodeId;
    private Long currentNodeId;
    private LocalDateTime departureEta;
    private LocalDateTime arrivalEta;
    private LocalDateTime actualDepartedAt;
    private LocalDateTime actualArrivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(nullable = false)
    private boolean temperatureRequired;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null || this.publicId.isBlank()) {
            this.publicId = PublicIdGenerator.next();
        }
    }

//    출발 체크포인트를 통과했을 때 호출
    public void markInTransit(Long currentNodeId, LocalDateTime actualDepartedAt){
        this.currentNodeId = currentNodeId;
        this.actualDepartedAt = actualDepartedAt;
        this.status = ShipmentStatus.IN_TRANSIT;
    }

//    도착 체크포인트를 통과했을 때 호출
    public void markArrived(Long currentNodeId, LocalDateTime actualArrivedAt){
        this.currentNodeId = currentNodeId;
        this.actualArrivedAt = actualArrivedAt;
        this.status = ShipmentStatus.ARRIVED;
    }

//    상태X/위치만 바꿀 때
    public void updateCurrentNode(Long currentNodeId){
        this.currentNodeId = currentNodeId;
    }
}
