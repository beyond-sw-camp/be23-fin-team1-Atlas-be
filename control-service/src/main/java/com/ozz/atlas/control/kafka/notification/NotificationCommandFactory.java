package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.control.kafka.recommendation.RecommendationDecisionPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationFailedPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationGeneratedPayload;
import com.ozz.atlas.control.notification.command.NotificationCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationCommandFactory {

    private final ObjectMapper objectMapper;

    public List<NotificationCommand> create(
            EventEnvelope<JsonNode> eventEnvelope,
            List<String> recipientUserPublicIds
    ) {
        if (recipientUserPublicIds == null || recipientUserPublicIds.isEmpty()) {
            return List.of();
        }

        return recipientUserPublicIds.stream()
                .map(recipientUserPublicId -> createCommand(eventEnvelope, recipientUserPublicId))
                .toList();
    }

    private NotificationCommand createCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        return switch (eventEnvelope.eventType()) {
            case EventTypes.RECOMMENDATION_GENERATED ->
                    generatedCommand(eventEnvelope, recipientUserPublicId);
            case EventTypes.RECOMMENDATION_FAILED ->
                    failedCommand(eventEnvelope, recipientUserPublicId);
            case EventTypes.RECOMMENDATION_ACCEPTED,
                 EventTypes.RECOMMENDATION_REJECTED ->
                    decisionCommand(eventEnvelope, recipientUserPublicId);
            default -> throw new IllegalArgumentException("지원하지 않는 알림 이벤트입니다. eventType=" + eventEnvelope.eventType());
        };
    }

    private NotificationCommand generatedCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationGeneratedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationGeneratedPayload.class);

        String targetLabel = StringUtils.hasText(payload.shipmentPublicId())
                ? payload.shipmentPublicId() + " 출하"
                : payload.riskType() + " 리스크";

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .notificationType(DomainType.RISK)
                .title("권고안 생성이 완료되었습니다.")
                .message(targetLabel + "에 대한 권고안이 생성되었습니다.")
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }

    private NotificationCommand failedCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationFailedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationFailedPayload.class);

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .notificationType(DomainType.SYSTEM)
                .title("권고안 생성에 실패했습니다.")
                .message(payload.errorMessage())
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }

    private NotificationCommand decisionCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationDecisionPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationDecisionPayload.class);

        String title = EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                ? "권고안이 수락되었습니다."
                : "권고안이 거절되었습니다.";
        String message = EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                ? payload.shipmentPublicId() + " 출하 대응 권고안이 수락되었습니다."
                : payload.shipmentPublicId() + " 출하 대응 권고안이 거절되었습니다.";

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .notificationType(DomainType.RISK)
                .title(title)
                .message(message)
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }
}
