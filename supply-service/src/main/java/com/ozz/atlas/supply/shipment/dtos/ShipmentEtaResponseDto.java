package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.EtaBasis;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Shipment Eta 값 응답")
public class ShipmentEtaResponseDto {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ShipmentStatus status;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String currentNodePublicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String destinationNodePublicId;

    @Schema(description = "departure Eta 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime departureEta;
    @Schema(description = "arrival Eta 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime arrivalEta;
    @Schema(description = "actual Departed At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime actualDepartedAt;
    @Schema(description = "actual Arrived At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime actualArrivedAt;

//    최종계산 ETA
    @Schema(description = "estimated Arrival At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime estimatedArrivalAt;

//    몇 분 지연인지
    @Schema(description = "delay Minutes 값", example = "1", nullable = true)
    private Long delayMinutes;

    @Schema(description = "delayed 값", example = "true", nullable = true)
    private boolean delayed;
    @Schema(description = "eta Basis 값", example = "sample", nullable = true)
    private EtaBasis etaBasis;

//    마지막 이벤트 종류
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private CheckpointType lastCheckpointType;

//    마지막 통과 시간
    @Schema(description = "last Checkpoint At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime lastCheckpointAt;

//    마지막 노드 위치
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String lastCheckpointNodePublicId;

}
