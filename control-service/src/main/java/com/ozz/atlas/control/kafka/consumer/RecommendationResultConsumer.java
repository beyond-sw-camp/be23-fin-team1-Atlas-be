package com.ozz.atlas.control.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.control.kafka.notification.KafkaNotificationOrchestrator;
import com.ozz.atlas.control.kafka.processed.ProcessedEventService;
import com.ozz.atlas.control.kafka.recommendation.RecommendationDecisionPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationFailedPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationGeneratedPayload;
import com.ozz.atlas.control.recommendation.service.RecommendationService;
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
    private final KafkaNotificationOrchestrator kafkaNotificationOrchestrator;
    private final RecommendationService recommendationService;

    @KafkaListener(
            topics = {
                    KafkaTopics.CONTROL_RECOMMENDATION_DECISION,
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

            if (KafkaTopics.CONTROL_RECOMMENDATION_DECISION.equals(eventEnvelope.topic())) {
                handleDecision(eventEnvelope);
            } else if (KafkaTopics.CONTROL_RECOMMENDATION_GENERATED.equals(eventEnvelope.topic())) {
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

        // 결과 이벤트를 먼저 저장해두면 알림 발행 실패와 무관하게 권고안 조회/API는 일관되게 유지된다.
        recommendationService.saveGenerated(
                payload,
                eventEnvelope.actorUserPublicId(),
                eventEnvelope.organizationPublicId()
        );

        log.info("권고안 생성 완료 이벤트 수신. recommendationPublicId={}, shipmentPublicId={}",
                payload.recommendationPublicId(), payload.shipmentPublicId());
        kafkaNotificationOrchestrator.dispatch(eventEnvelope);
    }

    private void handleFailed(EventEnvelope<JsonNode> eventEnvelope) {
        RecommendationFailedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationFailedPayload.class);

        // 실패 결과도 동일한 recommendation 엔티티에 반영해 UI와 운영 로그에서 최종 상태를 바로 볼 수 있게 한다.
        recommendationService.saveFailed(
                payload,
                eventEnvelope.actorUserPublicId(),
                eventEnvelope.organizationPublicId()
        );

        log.warn("권고안 생성 실패 이벤트 수신. recommendationPublicId={}, error={}",
                payload.recommendationPublicId(), payload.errorMessage());
        kafkaNotificationOrchestrator.dispatch(eventEnvelope);
    }

    private void handleDecision(EventEnvelope<JsonNode> eventEnvelope) {
        RecommendationDecisionPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationDecisionPayload.class);

        log.info("권고안 의사결정 이벤트 수신. recommendationPublicId={}, eventType={}",
                payload.recommendationPublicId(), eventEnvelope.eventType());
        kafkaNotificationOrchestrator.dispatch(eventEnvelope);
    }
}
