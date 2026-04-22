package com.ozz.atlas.control.kafka.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "권고안 생성 요청 이벤트 payload")
public record RecommendationRequestedPayload(
        @Schema(description = "원본 이벤트 ID", example = "01KPPPEBTZRBJ02YPGB0XHWETC")
        String sourceEventId,
        @Schema(description = "원본 이벤트 타입", example = "shipment.delay-detected")
        String sourceEventType,
        @Schema(description = "리스크 유형", example = "shipment_delayed")
        String riskType,
        @Schema(description = "출하 공개 식별자", example = "01KPPPDYPH4Z7J4MYZ1439KNBA")
        String shipmentPublicId,
        @Schema(description = "출하 번호", example = "SHIP-E2E-0003")
        String shipmentNumber,
        @Schema(description = "지연 분 수", example = "50")
        long delayMinutes,
        @Schema(description = "예정 도착 시각", example = "2026-04-21T10:00:00")
        LocalDateTime arrivalEta,
        @Schema(description = "추정 도착 시각", example = "2026-04-21T10:50:00", nullable = true)
        LocalDateTime estimatedArrivalAt,
        @Schema(description = "현재 물류 노드 공개 식별자", example = "01KPPPDNJVFNETV6Y28NAN673M", nullable = true)
        String currentNodePublicId,
        @Schema(description = "권고안 생성 요청 요약", example = "출하 지연이 감지되어 대응 권고안 생성이 필요합니다.")
        String summary
) {
}
