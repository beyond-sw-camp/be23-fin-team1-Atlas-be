package com.ozz.atlas.control.kafka.rule.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kafka_event_rule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaEventRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kafka_event_rule_id")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(name = "rule_code", nullable = false, unique = true, length = 20)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 120)
    private String ruleName;

    @Column(name = "topic", nullable = false, length = 120)
    private String topic;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "condition_text", nullable = false, length = 200)
    private String condition;

    @Column(name = "threshold_text", nullable = false, length = 200)
    private String threshold;

    @Column(name = "triggered_count", nullable = false)
    @Builder.Default
    private long triggeredCount = 0L;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    private KafkaRuleImportance importance;

    @Column(name = "updated_by_user_public_id", length = 26)
    private String updatedByUserPublicId;

    public static KafkaEventRule create(
            String ruleCode,
            String ruleName,
            String topic,
            String eventType,
            String condition,
            String threshold,
            KafkaRuleImportance importance,
            boolean enabled,
            String updatedByUserPublicId
    ) {
        return KafkaEventRule.builder()
                .ruleCode(ruleCode)
                .ruleName(ruleName)
                .topic(topic)
                .eventType(eventType)
                .condition(condition)
                .threshold(threshold)
                .importance(importance)
                .enabled(enabled)
                .updatedByUserPublicId(updatedByUserPublicId)
                .build();
    }

    public void update(
            String ruleName,
            String topic,
            String eventType,
            String condition,
            String threshold,
            KafkaRuleImportance importance,
            boolean enabled,
            String updatedByUserPublicId
    ) {
        this.ruleName = ruleName;
        this.topic = topic;
        this.eventType = eventType;
        this.condition = condition;
        this.threshold = threshold;
        this.importance = importance;
        this.enabled = enabled;
        this.updatedByUserPublicId = updatedByUserPublicId;
    }

    public void changeEnabled(boolean enabled, String updatedByUserPublicId) {
        this.enabled = enabled;
        this.updatedByUserPublicId = updatedByUserPublicId;
    }

    public void increaseTriggeredCount() {
        this.triggeredCount += 1;
    }
}
