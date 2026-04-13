package com.ozz.atlas.supply.shipment.domain;

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
public class ShipmentCheckpoint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shipmentId;

    @Column(nullable = false)
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CheckpointType checkpointType;

    private LocalDateTime plannedAt;
    private LocalDateTime actualAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckpointStatus checkpointStatus;

    @Column(length = 255)
    private String note;
}
