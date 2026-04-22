package com.ozz.atlas.control.kafka.processed;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "processed_event",
        indexes = {
                @Index(name = "idx_processed_event_event_id", columnList = "event_id", unique = true),
                @Index(name = "idx_processed_event_event_type", columnList = "event_type")
        }
)
public class ProcessedEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processed_event_id")
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, updatable = false, length = 26)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 120)
    private String topic;

    @Column(name = "aggregate_public_id", length = 26)
    private String aggregatePublicId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Builder
    private ProcessedEvent(
            String eventId,
            String eventType,
            String topic,
            String aggregatePublicId,
            LocalDateTime processedAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.aggregatePublicId = aggregatePublicId;
        this.processedAt = processedAt;
    }

    public static ProcessedEvent of(
            String eventId,
            String eventType,
            String topic,
            String aggregatePublicId
    ) {
        return ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .topic(topic)
                .aggregatePublicId(aggregatePublicId)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
