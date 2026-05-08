package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Eta Projection 값 응답")
public class EtaProjectionResponseDto {

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long riskEventId;
    @Schema(description = "previous Eta 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime previousEta;
    @Schema(description = "projected Eta 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime projectedEta;
    @Schema(description = "delay Minutes 값", example = "1", nullable = true)
    private Long delayMinutes;
    @Schema(description = "calculated At 값", example = "2026-05-08T10:00:00", nullable = true)
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
