package com.ozz.atlas.control.kafka.log;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.control.kafka.outbox.OutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "event_log",
        indexes = {
                @Index(name = "idx_event_log_event_type", columnList = "event_type"),
                @Index(name = "idx_event_log_aggregate", columnList = "aggregate_type,aggregate_public_id")
        }
)
public class EventLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_log_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String eventId;

    @Column(nullable = false, length = 120)
    private String topic;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AggregateType aggregateType;

    @Column(nullable = false, length = 26)
    private String aggregatePublicId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventLogStatus status;

    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Builder
    private EventLog(
            String eventId,
            String topic,
            String eventType,
            AggregateType aggregateType,
            String aggregatePublicId,
            String eventJson,
            EventLogStatus status,
            LocalDateTime publishedAt,
            String lastError
    ) {
        this.eventId = eventId;
        this.topic = topic;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregatePublicId = aggregatePublicId;
        this.eventJson = eventJson;
        this.status = status;
        this.publishedAt = publishedAt;
        this.lastError = lastError;
    }

    public static EventLog publishedFrom(OutboxEvent outboxEvent) {
        // event_log는 재시도용 큐가 아니라 최종 발행 결과만 남기는 이력 테이블이다.
        return EventLog.builder()
                .eventId(outboxEvent.getEventId())
                .topic(outboxEvent.getTopic())
                .eventType(outboxEvent.getEventType())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregatePublicId(outboxEvent.getAggregatePublicId())
                .eventJson(outboxEvent.getEventJson())
                .status(EventLogStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build();
    }

    public static EventLog failedFrom(OutboxEvent outboxEvent, String lastError) {
        return EventLog.builder()
                .eventId(outboxEvent.getEventId())
                .topic(outboxEvent.getTopic())
                .eventType(outboxEvent.getEventType())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregatePublicId(outboxEvent.getAggregatePublicId())
                .eventJson(outboxEvent.getEventJson())
                .status(EventLogStatus.FAILED)
                .lastError(lastError)
                .build();
    }

    public void markPublished(OutboxEvent outboxEvent) {
        this.status = EventLogStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.lastError = null;
        this.eventJson = outboxEvent.getEventJson();
    }

    public void markFailed(OutboxEvent outboxEvent, String lastError) {
        this.status = EventLogStatus.FAILED;
        this.lastError = lastError;
        this.eventJson = outboxEvent.getEventJson();
    }
}
