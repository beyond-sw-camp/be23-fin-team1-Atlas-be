package com.ozz.atlas.supply.shipment.dtos;

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
public class TrackShipmentRequestDto {

    @NotBlank
    private String nodePublicId;

    @NotNull
    private CheckpointType checkpointType;

    @NotNull
    private CheckpointStatus checkpointStatus;

    @NotNull
    private LocalDateTime plannedAt;

    private LocalDateTime actualAt;

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
