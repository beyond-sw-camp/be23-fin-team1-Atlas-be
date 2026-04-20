package com.ozz.atlas.control.event.recommendation;

public record RecommendationFailedPayload(
        String sourceEventId,
        String sourceEventType,
        String recommendationPublicId,
        String shipmentPublicId,
        String riskType,
        String errorMessage
) {
}
