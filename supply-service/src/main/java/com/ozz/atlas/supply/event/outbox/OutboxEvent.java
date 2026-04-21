package com.ozz.atlas.supply.event.outbox;

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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
        name = "outbox_event",
        indexes = {
                // 발행 스케줄러가 다음 재시도 대상을 빠르게 찾기 위한 인덱스
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
    @Builder.Default
    private String eventId = PublicIdGenerator.next();

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
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String eventJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "next_attempt_at", nullable = false)
    @Builder.Default
    private LocalDateTime nextAttemptAt = LocalDateTime.now();

    @Column(name = "last_attempted_at")
    private LocalDateTime lastAttemptedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

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
        // 서비스 트랜잭션 안에서는 Kafka로 직접 보내지 않고, 먼저 outbox_event에 발행 대기 상태로 적재한다.
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

    public void markFailed(String lastError, LocalDateTime nextAttemptAt) {
        // 실패한 이벤트는 지우지 않고 재시도 대상으로 남긴다.
        this.status = OutboxEventStatus.FAILED;
        this.retryCount = this.retryCount + 1;
        this.lastError = lastError;
        this.lastAttemptedAt = LocalDateTime.now();
        this.nextAttemptAt = nextAttemptAt;
    }
}
