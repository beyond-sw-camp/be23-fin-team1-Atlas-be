package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 수정 요청")
public class UpdateShipmentRequestDto {

    @Schema(description = "운송사명", example = "CJ Logistics", nullable = true)
    private String carrierName;

    @Schema(description = "차량 번호", example = "12가3456", nullable = true)
    private String vehicleNo;

    @Schema(description = "운송 추적 번호", example = "TRK-ATLAS-20260417", nullable = true)
    private String trackingNo;

    @Schema(description = "출발 물류 거점 공개 식별자", example = "node_origin_01HZY1AAA", nullable = true)
    private String originNodePublicId;

    @Schema(description = "도착 물류 거점 공개 식별자", example = "node_dest_01HZY1BBB", nullable = true)
    private String destinationNodePublicId;

    @Schema(description = "예상 출발 시각", example = "2026-04-18T08:00:00", nullable = true)
    private LocalDateTime departureEta;

    @Schema(description = "예상 도착 시각", example = "2026-04-18T14:00:00", nullable = true)
    private LocalDateTime arrivalEta;
}
