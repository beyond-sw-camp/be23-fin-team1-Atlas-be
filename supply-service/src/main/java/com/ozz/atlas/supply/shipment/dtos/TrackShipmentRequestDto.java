package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.CheckpointStatus;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
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

    @NotNull
    private Long nodeId;

    @NotNull
    private CheckpointType checkpointType;

    @NotNull
    private CheckpointStatus checkpointStatus;

    private LocalDateTime plannedAt;
    private LocalDateTime actualAt;
    private String note;
}
