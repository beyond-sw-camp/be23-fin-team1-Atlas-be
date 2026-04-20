package com.ozz.atlas.control.event.recommendation;

import java.time.LocalDateTime;

public record RecommendationRequestedPayload(
        String sourceEventId,
        String sourceEventType,
        String riskType,
        String shipmentPublicId,
        String shipmentNumber,
        long delayMinutes,
        LocalDateTime arrivalEta,
        LocalDateTime estimatedArrivalAt,
        String currentNodePublicId,
        String summary
) {
}
