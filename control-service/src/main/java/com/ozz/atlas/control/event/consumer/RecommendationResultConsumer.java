package com.ozz.atlas.control.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.event.EventEnvelope;
import com.ozz.atlas.common.event.KafkaTopics;
import com.ozz.atlas.control.event.processed.ProcessedEventService;
import com.ozz.atlas.control.event.recommendation.RecommendationFailedPayload;
import com.ozz.atlas.control.event.recommendation.RecommendationGeneratedPayload;
import com.ozz.atlas.control.notification.dto.NotificationDto;
import com.ozz.atlas.control.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationResultConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = {
                    KafkaTopics.CONTROL_RECOMMENDATION_GENERATED,
                    KafkaTopics.CONTROL_RECOMMENDATION_FAILED
            },
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeRecommendationResult(String message, Acknowledgment acknowledgment) {
        try {
            EventEnvelope<JsonNode> eventEnvelope = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventEnvelope.class, JsonNode.class)
            );

            if (processedEventService.isAlreadyProcessed(eventEnvelope.eventId())) {
                acknowledgment.acknowledge();
                return;
            }

            if (KafkaTopics.CONTROL_RECOMMENDATION_GENERATED.equals(eventEnvelope.topic())) {
                handleGenerated(eventEnvelope);
            } else if (KafkaTopics.CONTROL_RECOMMENDATION_FAILED.equals(eventEnvelope.topic())) {
                handleFailed(eventEnvelope);
            }

            processedEventService.markProcessed(
                    eventEnvelope.eventId(),
                    eventEnvelope.eventType(),
                    eventEnvelope.topic(),
                    eventEnvelope.aggregatePublicId()
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("권고안 결과 이벤트 소비에 실패했습니다.", e);
            throw new IllegalStateException("권고안 결과 이벤트 소비에 실패했습니다.", e);
        }
    }

    private void handleGenerated(EventEnvelope<JsonNode> eventEnvelope) {
        RecommendationGeneratedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationGeneratedPayload.class);

        log.info("권고안 생성 완료 이벤트 수신. recommendationPublicId={}, shipmentPublicId={}",
                payload.recommendationPublicId(), payload.shipmentPublicId());

        if (eventEnvelope.actorUserPublicId() == null || eventEnvelope.actorUserPublicId().isBlank()) {
            return;
        }

        notificationService.saveAndPublish(NotificationDto.builder()
                .recipientUserPublicId(eventEnvelope.actorUserPublicId())
                .notificationType(DomainType.RISK)
                .title("권고안 생성이 완료되었습니다.")
                .message("%s 출하 지연에 대한 권고안이 생성되었습니다.".formatted(payload.shipmentPublicId()))
                .deepLinkUrl("/recommendations/%s".formatted(payload.recommendationPublicId()))
                .referencePublicId(payload.recommendationPublicId())
                .build());
    }

    private void handleFailed(EventEnvelope<JsonNode> eventEnvelope) {
        RecommendationFailedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationFailedPayload.class);

        log.warn("권고안 생성 실패 이벤트 수신. recommendationPublicId={}, error={}",
                payload.recommendationPublicId(), payload.errorMessage());

        if (eventEnvelope.actorUserPublicId() == null || eventEnvelope.actorUserPublicId().isBlank()) {
            return;
        }

        notificationService.saveAndPublish(NotificationDto.builder()
                .recipientUserPublicId(eventEnvelope.actorUserPublicId())
                .notificationType(DomainType.SYSTEM)
                .title("권고안 생성에 실패했습니다.")
                .message(payload.errorMessage())
                .deepLinkUrl("/recommendations/%s".formatted(payload.recommendationPublicId()))
                .referencePublicId(payload.recommendationPublicId())
                .build());
    }
}
