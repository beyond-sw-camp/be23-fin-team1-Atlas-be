package com.ozz.atlas.control.kafka.monitoring.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaEventRuleToggleRequest {

    @Schema(description = "활성화 여부", example = "false")
    @NotNull(message = "활성화 여부는 필수입니다.")
    private Boolean enabled;
}
