package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.ozz.atlas.common.kafka.EventEnvelope;

public interface KafkaNotificationHandler {

    boolean supports(String eventType);

    void handle(EventEnvelope<JsonNode> eventEnvelope);
}
