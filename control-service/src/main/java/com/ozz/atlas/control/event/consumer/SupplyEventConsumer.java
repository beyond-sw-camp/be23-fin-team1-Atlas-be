package com.ozz.atlas.control.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.event.EventEnvelope;
import com.ozz.atlas.common.event.EventTypes;
import com.ozz.atlas.common.event.KafkaTopics;
import com.ozz.atlas.control.event.processed.ProcessedEventService;
import com.ozz.atlas.control.event.outbox.OutboxEventAppender;
import com.ozz.atlas.control.event.recommendation.RecommendationEventFactory;
import com.ozz.atlas.control.event.shipment.ShipmentCreatedPayload;
import com.ozz.atlas.control.event.shipment.ShipmentDelayDetectedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final OutboxEventAppender outboxEventAppender;
    private final RecommendationEventFactory recommendationEventFactory;

    @KafkaListener(
            topics = KafkaTopics.SUPPLY_SHIPMENT,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeShipmentEvent(String message, Acknowledgment acknowledgment) {
        try {
            EventEnvelope<JsonNode> eventEnvelope = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventEnvelope.class, JsonNode.class)
            );

            if (processedEventService.isAlreadyProcessed(eventEnvelope.eventId())) {
                // 같은 eventId를 다시 받았을 때는 비즈니스 처리를 건너뛰고 offset만 커밋한다.
                acknowledgment.acknowledge();
                return;
            }

            handleShipmentEvent(eventEnvelope);
            processedEventService.markProcessed(
                    eventEnvelope.eventId(),
                    eventEnvelope.eventType(),
                    eventEnvelope.topic(),
                    eventEnvelope.aggregatePublicId()
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Supply 이벤트 소비에 실패했습니다.", e);
            throw new IllegalStateException("Supply 이벤트 소비에 실패했습니다.", e);
        }
    }

    private void handleShipmentEvent(EventEnvelope<JsonNode> eventEnvelope) {
        switch (eventEnvelope.eventType()) {
            case EventTypes.SHIPMENT_CREATED -> handleShipmentCreated(eventEnvelope);
            case EventTypes.SHIPMENT_DELAY_DETECTED -> handleShipmentDelayDetected(eventEnvelope);
            default -> log.info("아직 처리 로직이 없는 supply 이벤트입니다. eventId={}, eventType={}",
                    eventEnvelope.eventId(), eventEnvelope.eventType());
        }
    }

    private void handleShipmentCreated(EventEnvelope<JsonNode> eventEnvelope) {
        ShipmentCreatedPayload payload = objectMapper.convertValue(eventEnvelope.payload(), ShipmentCreatedPayload.class);
        log.info("출하 생성 이벤트 수신. shipmentPublicId={}, purchaseOrderPublicId={}",
                payload.shipmentPublicId(), payload.purchaseOrderPublicId());
    }

    private void handleShipmentDelayDetected(EventEnvelope<JsonNode> eventEnvelope) {
        ShipmentDelayDetectedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), ShipmentDelayDetectedPayload.class);
        log.info("출하 지연 이벤트 수신. shipmentPublicId={}, delayMinutes={}",
                payload.shipmentPublicId(), payload.delayMinutes());
        // 지연 이벤트는 control-service 내부 recommendation.requested 이벤트로 한 번 더 변환한다.
        outboxEventAppender.append(recommendationEventFactory.shipmentDelayRequested(eventEnvelope, payload));
    }
}
