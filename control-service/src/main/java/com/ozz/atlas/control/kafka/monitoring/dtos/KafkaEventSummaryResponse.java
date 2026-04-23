package com.ozz.atlas.control.kafka.monitoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaEventSummaryResponse {

    private String ruleId;
    private String ruleName;
    private String topic;
    private String eventType;
    private String condition;
    private String threshold;
    private Integer triggeredCount;
    private boolean enabled;
    private String importance;
}
