package com.ozz.atlas.common.event;

import java.time.Instant;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String schemaVersion,
        String producer,
        String topic,
        AggregateType aggregateType,
        String aggregatePublicId,
        String partitionKey,
        Instant occurredAt,
        String correlationId,
        String causationId,
        String actorUserPublicId,
        String organizationPublicId,
        T payload
) {
}
