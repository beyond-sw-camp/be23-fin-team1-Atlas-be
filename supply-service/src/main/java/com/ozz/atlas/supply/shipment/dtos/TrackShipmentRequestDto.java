package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.shipment.domain.CheckpointStatus;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.ShipmentCheckpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Track Shipment 값 요청")
public class TrackShipmentRequestDto {

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String nodePublicId;

    @NotNull
    @Schema(description = "유형", example = "DEFAULT")
    private CheckpointType checkpointType;

    @NotNull
    @Schema(description = "상태", example = "ACTIVE")
    private CheckpointStatus checkpointStatus;

    @NotNull
    @Schema(description = "planned At 값", example = "2026-05-08T10:00:00")
    private LocalDateTime plannedAt;

    @Schema(description = "actual At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime actualAt;

    @Schema(description = "note 값", example = "sample", nullable = true)
    private String note;
    public ShipmentCheckpoint toEntity(Long shipmentId, Long nodeId) {
        return ShipmentCheckpoint.builder()
                .shipmentId(shipmentId)
                .nodeId(nodeId)
                .checkpointType(this.checkpointType)
                .checkpointStatus(this.checkpointStatus)
                .plannedAt(this.plannedAt)
                .actualAt(this.actualAt)
                .note(this.note)
                .build();
    }
}
