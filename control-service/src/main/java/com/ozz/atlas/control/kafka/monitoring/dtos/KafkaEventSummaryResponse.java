package com.ozz.atlas.control.kafka.monitoring.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kafka Event Summary 값 응답")
public class KafkaEventSummaryResponse {

    @Schema(description = "식별자", example = "1", nullable = true)
    private String ruleId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String ruleName;
    @Schema(description = "topic 값", example = "sample", nullable = true)
    private String topic;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String eventType;
    @Schema(description = "condition 값", example = "sample", nullable = true)
    private String condition;
    @Schema(description = "threshold 값", example = "sample", nullable = true)
    private String threshold;
    @Schema(description = "개수", example = "1", nullable = true)
    private Integer triggeredCount;
    @Schema(description = "enabled 값", example = "true", nullable = true)
    private boolean enabled;
    @Schema(description = "importance 값", example = "sample", nullable = true)
    private String importance;
}
