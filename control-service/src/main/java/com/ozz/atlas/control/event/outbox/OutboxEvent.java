package com.ozz.atlas.control.event.outbox;

import com.ozz.atlas.common.event.AggregateType;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "outbox_event",
        indexes = {
                // 스케줄러가 발행 가능한 이벤트를 빠르게 집계하기 위한 인덱스
                @Index(name = "idx_outbox_event_status_next_attempt", columnList = "status,next_attempt_at"),
                @Index(name = "idx_outbox_event_aggregate", columnList = "aggregate_type,aggregate_public_id")
        }
)
public class OutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_event_id")
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_attempted_at")
    private LocalDateTime lastAttemptedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Builder
    private OutboxEvent(
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
            OutboxEventStatus status,
            Integer retryCount,
            LocalDateTime nextAttemptAt
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
        this.nextAttemptAt = nextAttemptAt;
    }

    public static OutboxEvent pending(
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
            String eventJson
    ) {
        // control-service도 도메인 처리 중에는 Kafka로 직접 보내지 않고 outbox에 먼저 적재한다.
        return OutboxEvent.builder()
                .eventId(eventId)
                .topic(topic)
                .eventType(eventType)
                .schemaVersion(schemaVersion)
                .producer(producer)
                .aggregateType(aggregateType)
                .aggregatePublicId(aggregatePublicId)
                .partitionKey(partitionKey)
                .organizationPublicId(organizationPublicId)
                .actorUserPublicId(actorUserPublicId)
                .correlationId(correlationId)
                .causationId(causationId)
                .occurredAt(occurredAt)
                .eventJson(eventJson)
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .nextAttemptAt(LocalDateTime.now())
                .build();
    }

    @PrePersist
    void prePersist() {
        if (this.eventId == null || this.eventId.isBlank()) {
            this.eventId = PublicIdGenerator.next();
        }
        if (this.status == null) {
            this.status = OutboxEventStatus.PENDING;
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.nextAttemptAt == null) {
            this.nextAttemptAt = LocalDateTime.now();
        }
        if (this.occurredAt == null) {
            this.occurredAt = LocalDateTime.now();
        }
    }

    public void markFailed(String lastError, LocalDateTime nextAttemptAt) {
        // 실패 이벤트는 지우지 않고 재시도 큐에 남겨 다음 스케줄 사이클에서 다시 발행한다.
        this.status = OutboxEventStatus.FAILED;
        this.retryCount = this.retryCount + 1;
        this.lastError = lastError;
        this.lastAttemptedAt = LocalDateTime.now();
        this.nextAttemptAt = nextAttemptAt;
    }
}
