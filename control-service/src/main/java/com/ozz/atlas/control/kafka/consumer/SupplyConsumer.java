package com.ozz.atlas.control.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.control.kafka.processed.ProcessedEventService;
import com.ozz.atlas.control.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.control.kafka.notification.KafkaNotificationOrchestrator;
import com.ozz.atlas.control.kafka.recommendation.RecommendationFactory;
import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import com.ozz.atlas.control.kafka.shipment.ShipmentCreatedPayload;
import com.ozz.atlas.control.kafka.shipment.ShipmentDelayDetectedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final OutboxEventAppender outboxEventAppender;
    private final RecommendationFactory recommendationFactory;
    private final KafkaEventRuleService kafkaEventRuleService;
    private final KafkaNotificationOrchestrator kafkaNotificationOrchestrator;

    @KafkaListener(
            topics = {
                    KafkaTopics.SUPPLY_PURCHASE_ORDER,
                    KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER,
                    KafkaTopics.SUPPLY_SHIPMENT,
                    KafkaTopics.SUPPLY_DELIVERY_EXCEPTION,
                    KafkaTopics.SUPPLY_LOGISTICS_NODE,
                    KafkaTopics.SUPPLY_INVENTORY,
                    KafkaTopics.SUPPLY_RETURN_REQUEST,
                    KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE,
                    KafkaTopics.SUPPLY_SUPPLIER_RISK
            },
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeSupplyEvent(String message, Acknowledgment acknowledgment) {
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

            handleSupplyEvent(eventEnvelope);
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

    private void handleSupplyEvent(EventEnvelope<JsonNode> eventEnvelope) {
        switch (eventEnvelope.eventType()) {
            case EventTypes.SHIPMENT_CREATED -> handleShipmentCreated(eventEnvelope);
            case EventTypes.SHIPMENT_DELAY_DETECTED -> handleShipmentDelayDetected(eventEnvelope);
            default -> kafkaNotificationOrchestrator.dispatch(eventEnvelope);
        }
    }

    private void handleShipmentCreated(EventEnvelope<JsonNode> eventEnvelope) {
        ShipmentCreatedPayload payload = objectMapper.convertValue(eventEnvelope.payload(), ShipmentCreatedPayload.class);
        log.info("출하 생성 이벤트 수신. shipmentPublicId={}, purchaseOrderPublicId={}",
                payload.shipmentPublicId(), payload.purchaseOrderPublicId());
        kafkaNotificationOrchestrator.dispatch(eventEnvelope);
    }

    private void handleShipmentDelayDetected(EventEnvelope<JsonNode> eventEnvelope) {
        ShipmentDelayDetectedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), ShipmentDelayDetectedPayload.class);
        log.info("출하 지연 이벤트 수신. shipmentPublicId={}, delayMinutes={}",
                payload.shipmentPublicId(), payload.delayMinutes());
        if (!kafkaEventRuleService.isEnabled(EventTypes.SHIPMENT_DELAY_DETECTED)) {
            log.info("비활성화된 Kafka 이벤트 규칙입니다. eventType={}", EventTypes.SHIPMENT_DELAY_DETECTED);
            return;
        }

        // 지연 이벤트는 control-service 내부 recommendation.requested 이벤트로 한 번 더 변환한다.
        outboxEventAppender.append(recommendationFactory.shipmentDelayRequested(eventEnvelope, payload));
        kafkaNotificationOrchestrator.dispatch(eventEnvelope);
    }
}
