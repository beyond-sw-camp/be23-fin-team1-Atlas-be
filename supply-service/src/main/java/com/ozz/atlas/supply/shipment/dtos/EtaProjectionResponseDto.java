package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.EtaProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class EtaProjectionResponseDto {

    private Long id;
    private Long riskEventId;
    private LocalDateTime previousEta;
    private LocalDateTime projectedEta;
    private Long delayMinutes;
    private LocalDateTime calculatedAt;

    public static EtaProjectionResponseDto from(EtaProjection etaProjection) {
        return EtaProjectionResponseDto.builder()
                .id(etaProjection.getId())
                .riskEventId(etaProjection.getRiskEventId())
                .previousEta(etaProjection.getPreviousEta())
                .projectedEta(etaProjection.getProjectedEta())
                .delayMinutes(etaProjection.getDelayMinutes())
                .calculatedAt(etaProjection.getCalculatedAt())
                .build();
    }
}
