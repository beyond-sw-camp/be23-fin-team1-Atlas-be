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
public class EtaProjection {

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
