package com.ozz.atlas.control.event.recommendation;

import java.util.List;

public record RecommendationGeneratedPayload(
        String sourceEventId,
        String sourceEventType,
        String recommendationPublicId,
        String shipmentPublicId,
        String riskType,
        String provider,
        String model,
        String modelVersion,
        List<RecommendationItemPayload> recommendations
) {
}
