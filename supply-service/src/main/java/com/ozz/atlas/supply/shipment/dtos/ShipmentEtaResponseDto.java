package com.ozz.atlas.supply.shipment.dtos;

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
public class ShipmentEtaResponseDto {

    private Long id;
    private ShipmentStatus status;
    private Long currentNodeId;
    private Long destinationNodeId;

    private LocalDateTime departureEta;
    private LocalDateTime arrivalEta;
    private LocalDateTime actualDepartedAt;
    private LocalDateTime actualArrivedAt;

//    최종계산 ETA
    private LocalDateTime estimatedArrivalAt;

//    몇 분 지연인지
    private Long delayMinutes;

    private boolean delayed;
    private EtaBasis etaBasis;

//    마지막 이벤트 종류
    private CheckpointType lastCheckpointType;

//    마지막 통과 시간
    private LocalDateTime lastCheckpointAt;

//    마지막 노드 위치
    private Long lastCheckpointNodeId;

}
