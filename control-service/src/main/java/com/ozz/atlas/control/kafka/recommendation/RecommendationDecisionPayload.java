package com.ozz.atlas.control.kafka.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "권고안 의사결정 이벤트 payload")
public record RecommendationDecisionPayload(
        @Schema(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9")
        String recommendationPublicId,
        @Schema(description = "출하 공개 식별자", example = "01KPPPDYPH4Z7J4MYZ1439KNBA")
        String shipmentPublicId,
        @Schema(description = "리스크 유형", example = "shipment_delayed")
        String riskType,
        @Schema(description = "의사결정 사용자 공개 식별자", example = "usr00000000000000000000001")
        String decidedByUserPublicId,
        @Schema(description = "의사결정 시각", example = "2026-04-21T15:20:00Z")
        Instant decidedAt
) {
}
