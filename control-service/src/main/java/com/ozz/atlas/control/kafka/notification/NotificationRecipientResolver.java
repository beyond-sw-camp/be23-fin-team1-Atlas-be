package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.control.kafka.recommendation.RecommendationDecisionPayload;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationRecipientResolver {

    private final ObjectMapper objectMapper;

    public List<String> resolve(EventEnvelope<JsonNode> eventEnvelope) {
        Set<String> recipientUserPublicIds = new LinkedHashSet<>();

        addIfHasText(recipientUserPublicIds, eventEnvelope.actorUserPublicId());

        if (EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                || EventTypes.RECOMMENDATION_REJECTED.equals(eventEnvelope.eventType())) {
            RecommendationDecisionPayload payload =
                    objectMapper.convertValue(eventEnvelope.payload(), RecommendationDecisionPayload.class);
            addIfHasText(recipientUserPublicIds, payload.decidedByUserPublicId());
        }

        return List.copyOf(recipientUserPublicIds);
    }

    private void addIfHasText(Set<String> recipientUserPublicIds, String userPublicId) {
        if (StringUtils.hasText(userPublicId)) {
            recipientUserPublicIds.add(userPublicId);
        }
    }
}
