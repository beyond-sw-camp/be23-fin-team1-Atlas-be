package com.ozz.atlas.supply.shipment.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
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
public class Shipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, unique = true, length = 50)
    private String shipmentNumber;

    private Long poId;

    private Long subPoId;

    @Column(length = 26)
    private String purchaseOrderPublicId;

    @Column(length = 26)
    private String subPurchaseOrderPublicId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ShipmentSourceType sourceType = ShipmentSourceType.ORDER;

    @Column(length = 26)
    private String sourcePublicId;

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

    @Column(nullable = false)
    private boolean sealedPackagingRequired;

    @Column(nullable = false)
    private boolean fragile;

    //    출발 체크포인트를 통과했을 때 호출
    public void markInTransit(Long currentNodeId, LocalDateTime actualDepartedAt) {
        if (this.status == ShipmentStatus.ARRIVED || this.status == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 종료된 출하입니다.");
        }
        if (this.actualDepartedAt != null) {
            throw new IllegalStateException("이미 출발 처리된 출하입니다.");
        }

        this.currentNodeId = currentNodeId;
        this.actualDepartedAt = actualDepartedAt;
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    //    도착 체크포인트를 통과했을 때 호출
    public void markArrived(Long currentNodeId, LocalDateTime actualArrivedAt) {
        if (this.status == ShipmentStatus.ARRIVED || this.status == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 종료된 출하입니다.");
        }
        if (this.actualDepartedAt == null) {
            throw new IllegalStateException("출발 처리되지 않은 출하는 도착 처리할 수 없습니다.");
        }
        if (this.actualArrivedAt != null) {
            throw new IllegalStateException("이미 도착 처리된 출하입니다.");
        }

        this.currentNodeId = currentNodeId;
        this.actualArrivedAt = actualArrivedAt;
        this.status = ShipmentStatus.ARRIVED;
    }

    //    상태X/위치만 바꿀 때
    public void updateCurrentNode(Long currentNodeId){
        this.currentNodeId = currentNodeId;
    }

//    DELAY 반영
    public void markDelayed() {
        if (this.status == ShipmentStatus.ARRIVED || this.status == ShipmentStatus.CANCELLED) {
            return;
        }
        this.status = ShipmentStatus.DELAYED;
    }

    public void cancel() {
        if (this.status != ShipmentStatus.READY) {
            throw new IllegalStateException("READY 상태의 출하만 취소할 수 있습니다.");
        }

        this.status = ShipmentStatus.CANCELLED;
    }

    public void updateShipmentInfo(
            String carrierName,
            String vehicleNo,
            String trackingNo,
            Long originNodeId,
            Long destinationNodeId,
            Long currentNodeId,
            LocalDateTime departureEta,
            LocalDateTime arrivalEta
    ) {
        this.carrierName = carrierName;
        this.vehicleNo = vehicleNo;
        this.trackingNo = trackingNo;
        this.originNodeId = originNodeId;
        this.destinationNodeId = destinationNodeId;
        this.currentNodeId = currentNodeId;
        this.departureEta = departureEta;
        this.arrivalEta = arrivalEta;
    }

    public void updateShipmentOptions(
            boolean temperatureRequired,
            boolean sealedPackagingRequired,
            boolean fragile
    ) {
        this.temperatureRequired = temperatureRequired;
        this.sealedPackagingRequired = sealedPackagingRequired;
        this.fragile = fragile;
    }

}
