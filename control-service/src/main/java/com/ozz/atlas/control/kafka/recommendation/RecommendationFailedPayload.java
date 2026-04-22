package com.ozz.atlas.control.kafka.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권고안 생성 실패 이벤트 payload")
public record RecommendationFailedPayload(
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
        @Schema(description = "실패 메시지", example = "모델 응답 생성에 실패했습니다.")
        String errorMessage
) {
}
