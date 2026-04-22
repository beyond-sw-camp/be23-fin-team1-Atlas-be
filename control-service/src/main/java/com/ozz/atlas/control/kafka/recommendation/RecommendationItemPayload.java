package com.ozz.atlas.control.kafka.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권고안 개별 항목 payload")
public record RecommendationItemPayload(
        @Schema(description = "권고안 제목", example = "대체 허브 경유로 배송 경로 변경")
        String title,
        @Schema(description = "권고안 근거", example = "현재 허브 적체로 예상 도착 지연이 커지고 있습니다.")
        String reason,
        @Schema(description = "실행 액션 제안", example = "부산 허브 대신 대전 허브로 경로를 우회합니다.")
        String action,
        @Schema(description = "우선순위", example = "1")
        int priority,
        @Schema(description = "신뢰도", example = "0.87")
        double confidence
) {
}
