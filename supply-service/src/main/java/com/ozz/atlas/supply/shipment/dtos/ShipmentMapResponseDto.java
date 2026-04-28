package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import com.ozz.atlas.supply.shipment.domain.EtaBasis;
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
@Schema(description = "출하 지도 전용 응답")
public class ShipmentMapResponseDto {

    @Schema(description = "출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789")
    private String publicId;

    @Schema(description = "출하 번호", example = "SHIP-PO-20260424-0001-001")
    private String shipmentNumber;

    @Schema(description = "상위 발주 공개 식별자", example = "po_01HZY1PO123456789", nullable = true)
    private String purchaseOrderPublicId;

    @Schema(description = "하위 발주 공개 식별자", example = "subpo_01HZY1SUBPO123456789", nullable = true)
    private String subPurchaseOrderPublicId;

    @Schema(description = "운송사명", example = "CJ Logistics", nullable = true)
    private String carrierName;

    @Schema(description = "차량 번호", example = "12가3456", nullable = true)
    private String vehicleNo;

    @Schema(description = "운송 추적 번호", example = "TRK-ATLAS-20260417", nullable = true)
    private String trackingNo;

    @Schema(description = "출하 상태", example = "IN_TRANSIT")
    private ShipmentStatus status;

    @Schema(description = "출발 거점 공개 식별자", example = "node_origin_01HZY1AAA")
    private String originNodePublicId;

    @Schema(description = "출발 거점명", example = "서울 물류창고")
    private String originNodeName;

    @Schema(description = "출발 거점 코드", example = "WH-CHO1-003")
    private String originNodeCode;

    @Schema(description = "출발 위도", example = "37.5000242")
    private BigDecimal originLatitude;

    @Schema(description = "출발 경도", example = "127.0365086")
    private BigDecimal originLongitude;

    @Schema(description = "도착 거점 공개 식별자", example = "node_dest_01HZY1BBB")
    private String destinationNodePublicId;

    @Schema(description = "도착 거점명", example = "인천 물류창고")
    private String destinationNodeName;

    @Schema(description = "도착 거점 코드", example = "WH-BUY1-001")
    private String destinationNodeCode;

    @Schema(description = "도착 위도", example = "37.3821549")
    private BigDecimal destinationLatitude;

    @Schema(description = "도착 경도", example = "126.6474942")
    private BigDecimal destinationLongitude;

    @Schema(description = "현재 거점 공개 식별자", example = "node_current_01HZY1CCC", nullable = true)
    private String currentNodePublicId;

    @Schema(description = "현재 거점명", example = "대전 허브", nullable = true)
    private String currentNodeName;

    @Schema(description = "현재 거점 코드", example = "WH-HUB1-002", nullable = true)
    private String currentNodeCode;

    @Schema(description = "현재 위도", example = "36.3504119", nullable = true)
    private BigDecimal currentLatitude;

    @Schema(description = "현재 경도", example = "127.3845475", nullable = true)
    private BigDecimal currentLongitude;

    @Schema(description = "예상 출발 시각", example = "2026-04-25T09:00:00")
    private LocalDateTime departureEta;

    @Schema(description = "예상 도착 시각", example = "2026-04-25T14:00:00")
    private LocalDateTime arrivalEta;

    @Schema(description = "실제 출발 시각", example = "2026-04-25T09:05:00", nullable = true)
    private LocalDateTime actualDepartedAt;

    @Schema(description = "실제 도착 시각", example = "2026-04-25T13:48:00", nullable = true)
    private LocalDateTime actualArrivedAt;

    @Schema(description = "현재 계산 ETA", example = "2026-04-25T13:55:00", nullable = true)
    private LocalDateTime estimatedArrivalAt;

    @Schema(description = "지연 여부", example = "false")
    private boolean delayed;

    @Schema(description = "지연 분", example = "15", nullable = true)
    private Long delayMinutes;

    @Schema(description = "ETA 계산 기준", example = "CHECKPOINT", nullable = true)
    private EtaBasis etaBasis;

    @Schema(description = "마지막 체크포인트 유형", example = "TRANSIT", nullable = true)
    private CheckpointType lastCheckpointType;

    @Schema(description = "마지막 체크포인트 시각", example = "2026-04-25T11:20:00", nullable = true)
    private LocalDateTime lastCheckpointAt;

    @Schema(description = "지도용 체크포인트 목록")
    private List<ShipmentMapCheckpointDto> checkpoints;
}
