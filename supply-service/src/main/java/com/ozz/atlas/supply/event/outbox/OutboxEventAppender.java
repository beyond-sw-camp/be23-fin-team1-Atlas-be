package com.ozz.atlas.supply.event.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.event.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventAppender {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void append(EventEnvelope<?> eventEnvelope) {
        String eventJson = serialize(eventEnvelope);

        OutboxEvent outboxEvent = OutboxEvent.pending(
                eventEnvelope.eventId(),
                eventEnvelope.topic(),
                eventEnvelope.eventType(),
                eventEnvelope.schemaVersion(),
                eventEnvelope.producer(),
                eventEnvelope.aggregateType(),
                eventEnvelope.aggregatePublicId(),
                eventEnvelope.partitionKey(),
                eventEnvelope.organizationPublicId(),
                eventEnvelope.actorUserPublicId(),
                eventEnvelope.correlationId(),
                eventEnvelope.causationId(),
                LocalDateTime.ofInstant(eventEnvelope.occurredAt(), ZoneOffset.UTC),
                eventJson
        );

        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(EventEnvelope<?> eventEnvelope) {
        try {
            return objectMapper.writeValueAsString(eventEnvelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 JSON 직렬화에 실패했습니다.", e);
        }
    }
}
