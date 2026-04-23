package com.ozz.atlas.control.kafka.monitoring.dtos;

import com.ozz.atlas.control.kafka.rule.domain.KafkaRuleImportance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaEventSummaryUpsertRequest {

    @Schema(description = "규칙 코드", example = "SHP-005")
    @NotBlank(message = "규칙 코드는 필수입니다.")
    private String ruleId;

    @Schema(description = "규칙명", example = "출하 지연 감지")
    @NotBlank(message = "규칙명은 필수입니다.")
    private String ruleName;

    @Schema(description = "Kafka 토픽", example = "atlas.supply.shipment")
    @NotBlank(message = "Kafka 토픽은 필수입니다.")
    private String topic;

    @Schema(description = "이벤트 타입", example = "shipment.delay-detected")
    @NotBlank(message = "이벤트 타입은 필수입니다.")
    private String eventType;

    @Schema(description = "조건 문구", example = "지연 시간 >=")
    @NotBlank(message = "조건 문구는 필수입니다.")
    private String condition;

    @Schema(description = "임계값", example = "24h")
    @NotBlank(message = "임계값은 필수입니다.")
    private String threshold;

    @Schema(description = "중요도", example = "HIGH")
    @NotNull(message = "중요도는 필수입니다.")
    private KafkaRuleImportance importance;

    @Schema(description = "활성화 여부", example = "true")
    private boolean enabled;
}
