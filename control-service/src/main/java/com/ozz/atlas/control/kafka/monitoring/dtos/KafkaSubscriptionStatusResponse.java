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
@Schema(description = "상태 응답")
public class KafkaSubscriptionStatusResponse {

    @Schema(description = "topic 값", example = "sample", nullable = true)
    private String topic;
    @Schema(description = "개수", example = "1", nullable = true)
    private int partitionCount;
    @Schema(description = "committed Offset 값", example = "1", nullable = true)
    private long committedOffset;
    @Schema(description = "end Offset 값", example = "1", nullable = true)
    private long endOffset;
    @Schema(description = "lag 값", example = "1", nullable = true)
    private long lag;
    @Schema(description = "메시지", example = "샘플 내용", nullable = true)
    private Integer messagesPerHour;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String brokerConnectionStatus;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String consumerSubscriptionStatus;

}
