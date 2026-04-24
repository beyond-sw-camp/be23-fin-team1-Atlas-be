package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.ozz.atlas.common.kafka.EventEnvelope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationOrchestrator {

    private final List<KafkaNotificationHandler> handlers;

    public void dispatch(EventEnvelope<JsonNode> eventEnvelope) {
        handlers.stream()
                .filter(handler -> handler.supports(eventEnvelope.eventType()))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.handle(eventEnvelope),
                        () -> log.debug("알림 처리 핸들러가 없습니다. eventType={}", eventEnvelope.eventType())
                );
    }
}
