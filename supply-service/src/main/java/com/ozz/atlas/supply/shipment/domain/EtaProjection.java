package com.ozz.atlas.supply.shipment.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Entity
public class EtaProjection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shipmentId;
    private Long riskEventId;
    private LocalDateTime previousEta;
    private LocalDateTime projectedEta;
    private Long delayMinutes;
    private LocalDateTime calculatedAt;
}
