package com.ozz.atlas.control.event.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "권고안 생성 완료 이벤트 payload")
public record RecommendationGeneratedPayload(
        @Schema(description = "원본 요청 이벤트 ID", example = "01KPPPEBTZRBJ02YPGB0XHWETC")
        String sourceEventId,
        @Schema(description = "원본 요청 이벤트 타입", example = "recommendation.requested")
        String sourceEventType,
        @Schema(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9")
        String recommendationPublicId,
        @Schema(description = "출하 공개 식별자", example = "01KPPPDYPH4Z7J4MYZ1439KNBA")
        String shipmentPublicId,
        @Schema(description = "리스크 유형", example = "shipment_delayed")
        String riskType,
        @Schema(description = "AI 제공자", example = "openai")
        String provider,
        @Schema(description = "모델명", example = "gpt-5.4")
        String model,
        @Schema(description = "모델 버전", example = "2026-04-21")
        String modelVersion,
        @Schema(description = "생성된 권고안 항목 목록")
        List<RecommendationItemPayload> recommendations
) {
}
