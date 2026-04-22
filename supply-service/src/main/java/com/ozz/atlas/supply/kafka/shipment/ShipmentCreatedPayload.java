package com.ozz.atlas.supply.kafka.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "출하 생성 이벤트 payload")
public record ShipmentCreatedPayload(
        @Schema(description = "출하 공개 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KB")
        String shipmentPublicId,
        @Schema(description = "출하 번호", example = "SHIP-2026-0007")
        String shipmentNumber,
        @Schema(description = "상위 발주 공개 식별자", example = "po_01HZY1PO123456789", nullable = true)
        String purchaseOrderPublicId,
        @Schema(description = "하위 발주 공개 식별자", example = "subpo_01HZY1SUBPO123456789", nullable = true)
        String subPurchaseOrderPublicId,
        @Schema(description = "출발 물류 노드 공개 식별자", example = "node_01HS6Q1M4F4BCJ0YV7Z5X8W9KE")
        String originNodePublicId,
        @Schema(description = "도착 물류 노드 공개 식별자", example = "node_01HS6Q1M4F4BCJ0YV7Z5X8W9KF")
        String destinationNodePublicId,
        @Schema(description = "예정 출발 시각", example = "2026-04-20T18:00:00")
        LocalDateTime departureEta,
        @Schema(description = "예정 도착 시각", example = "2026-04-21T02:00:00")
        LocalDateTime arrivalEta,
        @Schema(description = "초기 출하 상태", example = "READY")
        String status,
        @Schema(description = "온도 관리 필요 여부", example = "true")
        boolean temperatureRequired
) {
}
