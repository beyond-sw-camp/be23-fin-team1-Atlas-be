package com.ozz.atlas.supply.event.log;

import com.ozz.atlas.common.event.AggregateType;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.event.outbox.OutboxEvent;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(nullable = false, length = 20)
    private String schemaVersion;

    @Column(nullable = false, length = 80)
    private String producer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AggregateType aggregateType;

    @Column(nullable = false, length = 26)
    private String aggregatePublicId;

    @Column(nullable = false, length = 100)
    private String partitionKey;

    @Column(length = 26)
    private String organizationPublicId;

    @Column(length = 26)
    private String actorUserPublicId;

    @Column(length = 26)
    private String correlationId;

    @Column(length = 26)
    private String causationId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String eventJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventLogStatus status;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = false)
    private LocalDateTime firstAttemptedAt;

    @Column(nullable = false)
    private LocalDateTime lastAttemptedAt;

    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Builder
    private EventLog(
            String eventId,
            String topic,
            String eventType,
            String schemaVersion,
            String producer,
            AggregateType aggregateType,
            String aggregatePublicId,
            String partitionKey,
            String organizationPublicId,
            String actorUserPublicId,
            String correlationId,
            String causationId,
            LocalDateTime occurredAt,
            String eventJson,
            EventLogStatus status,
            Integer retryCount,
            LocalDateTime firstAttemptedAt,
            LocalDateTime lastAttemptedAt,
            LocalDateTime publishedAt,
            String lastError
    ) {
        this.eventId = eventId;
        this.topic = topic;
        this.eventType = eventType;
        this.schemaVersion = schemaVersion;
        this.producer = producer;
        this.aggregateType = aggregateType;
        this.aggregatePublicId = aggregatePublicId;
        this.partitionKey = partitionKey;
        this.organizationPublicId = organizationPublicId;
        this.actorUserPublicId = actorUserPublicId;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.occurredAt = occurredAt;
        this.eventJson = eventJson;
        this.status = status;
        this.retryCount = retryCount;
        this.firstAttemptedAt = firstAttemptedAt;
        this.lastAttemptedAt = lastAttemptedAt;
        this.publishedAt = publishedAt;
        this.lastError = lastError;
    }

    public static EventLog publishedFrom(OutboxEvent outboxEvent) {
        // event_log는 "보낼 예정"이 아니라 "실제로 어떤 결과로 끝났는가"를 남기는 이력 테이블
        LocalDateTime now = LocalDateTime.now();
        return EventLog.builder()
                .eventId(outboxEvent.getEventId())
                .topic(outboxEvent.getTopic())
                .eventType(outboxEvent.getEventType())
                .schemaVersion(outboxEvent.getSchemaVersion())
                .producer(outboxEvent.getProducer())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregatePublicId(outboxEvent.getAggregatePublicId())
                .partitionKey(outboxEvent.getPartitionKey())
                .organizationPublicId(outboxEvent.getOrganizationPublicId())
                .actorUserPublicId(outboxEvent.getActorUserPublicId())
                .correlationId(outboxEvent.getCorrelationId())
                .causationId(outboxEvent.getCausationId())
                .occurredAt(outboxEvent.getOccurredAt())
                .eventJson(outboxEvent.getEventJson())
                .status(EventLogStatus.PUBLISHED)
                .retryCount(outboxEvent.getRetryCount())
                .firstAttemptedAt(now)
                .lastAttemptedAt(now)
                .publishedAt(now)
                .build();
    }

    public static EventLog failedFrom(OutboxEvent outboxEvent, String lastError) {
        LocalDateTime now = LocalDateTime.now();
        return EventLog.builder()
                .eventId(outboxEvent.getEventId())
                .topic(outboxEvent.getTopic())
                .eventType(outboxEvent.getEventType())
                .schemaVersion(outboxEvent.getSchemaVersion())
                .producer(outboxEvent.getProducer())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregatePublicId(outboxEvent.getAggregatePublicId())
                .partitionKey(outboxEvent.getPartitionKey())
                .organizationPublicId(outboxEvent.getOrganizationPublicId())
                .actorUserPublicId(outboxEvent.getActorUserPublicId())
                .correlationId(outboxEvent.getCorrelationId())
                .causationId(outboxEvent.getCausationId())
                .occurredAt(outboxEvent.getOccurredAt())
                .eventJson(outboxEvent.getEventJson())
                .status(EventLogStatus.FAILED)
                .retryCount(outboxEvent.getRetryCount())
                .firstAttemptedAt(now)
                .lastAttemptedAt(now)
                .lastError(lastError)
                .build();
    }

    public void markPublished(OutboxEvent outboxEvent) {
        LocalDateTime now = LocalDateTime.now();
        this.status = EventLogStatus.PUBLISHED;
        this.retryCount = outboxEvent.getRetryCount();
        this.lastAttemptedAt = now;
        this.publishedAt = now;
        this.lastError = null;
    }

    public void markFailed(OutboxEvent outboxEvent, String lastError) {
        this.status = EventLogStatus.FAILED;
        this.retryCount = outboxEvent.getRetryCount();
        this.lastAttemptedAt = LocalDateTime.now();
        this.lastError = lastError;
    }
}
