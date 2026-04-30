package com.ozz.atlas.control.kafka.log.search.dtos;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.control.kafka.log.EventLogStatus;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EventLogSearchResponse(
        Long id,
        String eventId,
        String topic,
        String eventType,
        AggregateType aggregateType,
        String aggregatePublicId,
        String eventJson,
        EventLogStatus status,
        LocalDateTime publishedAt,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
