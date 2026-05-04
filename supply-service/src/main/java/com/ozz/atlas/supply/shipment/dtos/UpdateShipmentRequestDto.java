package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 수정 요청")
public class UpdateShipmentRequestDto {

    @NotNull
    @Schema(description = "변경할 출발 예정 시각", example = "2026-04-18T08:00:00")
    private LocalDateTime departureEta;

    @Schema(description = "온도 관리 필요 여부", example = "true")
    private Boolean temperatureRequired;

    @Schema(description = "밀봉 포장 필요 여부", example = "true")
    private Boolean sealedPackagingRequired;

    @Schema(description = "파손 위험 여부", example = "true")
    private Boolean fragile;
}
