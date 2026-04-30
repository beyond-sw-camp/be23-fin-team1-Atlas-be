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
}
