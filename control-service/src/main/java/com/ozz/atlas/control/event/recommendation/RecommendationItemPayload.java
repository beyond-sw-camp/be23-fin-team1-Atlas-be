package com.ozz.atlas.control.event.recommendation;

public record RecommendationItemPayload(
        String title,
        String reason,
        String action,
        int priority,
        double confidence
) {
}
