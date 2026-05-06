package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.EtaBasis;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Shipment map response")
public class ShipmentMapResponseDto {

    @Schema(description = "Shipment public id", example = "ship_01HZY1SHIPMENT123456789")
    private String publicId;

    @Schema(description = "Shipment number", example = "SHIP-PO-20260424-0001-001")
    private String shipmentNumber;

    @Schema(description = "Shipment source type", example = "ORDER")
    private ShipmentSourceType sourceType;

    @Schema(description = "Source public id", example = "01HZY1SOURCE12345678901234", nullable = true)
    private String sourcePublicId;

    @Schema(description = "Purchase order public id", example = "po_01HZY1PO123456789", nullable = true)
    private String purchaseOrderPublicId;

    @Schema(description = "Sub purchase order public id", example = "subpo_01HZY1SUBPO123456789", nullable = true)
    private String subPurchaseOrderPublicId;

    @Schema(description = "Carrier name", example = "CJ Logistics", nullable = true)
    private String carrierName;

    @Schema(description = "Vehicle number", example = "12가3456", nullable = true)
    private String vehicleNo;

    @Schema(description = "Tracking number", example = "TRK-ATLAS-20260417", nullable = true)
    private String trackingNo;

    @Schema(description = "Shipment status", example = "IN_TRANSIT")
    private ShipmentStatus status;

    @Schema(description = "Origin node public id", example = "node_origin_01HZY1AAA")
    private String originNodePublicId;

    @Schema(description = "Origin node name", example = "서울 물류창고")
    private String originNodeName;

    @Schema(description = "Origin node code", example = "WH-CHO1-003")
    private String originNodeCode;

    @Schema(description = "Origin latitude", example = "37.5000242")
    private BigDecimal originLatitude;

    @Schema(description = "Origin longitude", example = "127.0365086")
    private BigDecimal originLongitude;

    @Schema(description = "Destination node public id", example = "node_dest_01HZY1BBB")
    private String destinationNodePublicId;

    @Schema(description = "Destination node name", example = "인천 물류창고")
    private String destinationNodeName;

    @Schema(description = "Destination node code", example = "WH-BUY1-001")
    private String destinationNodeCode;

    @Schema(description = "Destination latitude", example = "37.3821549")
    private BigDecimal destinationLatitude;

    @Schema(description = "Destination longitude", example = "126.6474942")
    private BigDecimal destinationLongitude;

    @Schema(description = "Current node public id", example = "node_current_01HZY1CCC", nullable = true)
    private String currentNodePublicId;

    @Schema(description = "Current node name", example = "대전 허브", nullable = true)
    private String currentNodeName;

    @Schema(description = "Current node code", example = "WH-HUB1-002", nullable = true)
    private String currentNodeCode;

    @Schema(description = "Current latitude", example = "36.3504119", nullable = true)
    private BigDecimal currentLatitude;

    @Schema(description = "Current longitude", example = "127.3845475", nullable = true)
    private BigDecimal currentLongitude;

    @Schema(description = "Departure ETA", example = "2026-04-25T09:00:00")
    private LocalDateTime departureEta;

    @Schema(description = "Arrival ETA", example = "2026-04-25T14:00:00")
    private LocalDateTime arrivalEta;

    @Schema(description = "Actual departed at", example = "2026-04-25T09:05:00", nullable = true)
    private LocalDateTime actualDepartedAt;

    @Schema(description = "Actual arrived at", example = "2026-04-25T13:48:00", nullable = true)
    private LocalDateTime actualArrivedAt;

    @Schema(description = "Estimated arrival at", example = "2026-04-25T13:55:00", nullable = true)
    private LocalDateTime estimatedArrivalAt;

    @Schema(description = "Delayed flag", example = "false")
    private boolean delayed;

    @Schema(description = "Delay minutes", example = "15", nullable = true)
    private Long delayMinutes;

    @Schema(description = "ETA basis", example = "CHECKPOINT", nullable = true)
    private EtaBasis etaBasis;

    @Schema(description = "Last checkpoint type", example = "TRANSIT", nullable = true)
    private CheckpointType lastCheckpointType;

    @Schema(description = "Last checkpoint at", example = "2026-04-25T11:20:00", nullable = true)
    private LocalDateTime lastCheckpointAt;

    @Schema(description = "Whether the current organization can update shipment info", example = "true")
    private boolean canUpdate;

    @Schema(description = "Whether the current organization can start delivery", example = "true")
    private boolean canStart;

    @Schema(description = "Whether the current organization can confirm arrival", example = "false")
    private boolean canArrive;

    @Schema(description = "Whether the current organization can cancel shipment", example = "true")
    private boolean canCancel;

    @Schema(description = "Whether the current organization can register tracking", example = "false")
    private boolean canTrack;

    @Schema(description = "Whether the current organization can register delivery exception", example = "true")
    private boolean canRegisterException;

    @Schema(description = "Map checkpoints")
    private List<ShipmentMapCheckpointDto> checkpoints;
}
