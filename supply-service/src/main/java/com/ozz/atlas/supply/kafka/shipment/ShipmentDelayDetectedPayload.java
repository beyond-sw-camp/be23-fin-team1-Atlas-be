package com.ozz.atlas.supply.kafka.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "출하 지연 감지 이벤트 payload")
public record ShipmentDelayDetectedPayload(
        @Schema(description = "출하 공개 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KB")
        String shipmentPublicId,
        @Schema(description = "출하 번호", example = "SHIP-2026-0007")
        String shipmentNumber,
        @Schema(description = "현재 출하 상태", example = "DELAYED")
        String status,
        @Schema(description = "지연 분 수", example = "45")
        long delayMinutes,
        @Schema(description = "예정 도착 시각", example = "2026-04-21T02:00:00")
        LocalDateTime arrivalEta,
        @Schema(description = "추정 도착 시각", example = "2026-04-21T02:45:00", nullable = true)
        LocalDateTime estimatedArrivalAt,
        @Schema(description = "현재 물류 노드 공개 식별자", example = "node_01HS6Q1M4F4BCJ0YV7Z5X8W9KG", nullable = true)
        String currentNodePublicId,
        String rootPurchaseOrderPublicId,
        String rootBuyerOrganizationPublicId,
        String directBuyerOrganizationPublicId,
        String directSupplierOrganizationPublicId,
        String parentPurchaseOrderPublicId,
        String subPurchaseOrderPublicId
) {
}
