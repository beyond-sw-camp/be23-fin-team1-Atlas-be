package com.ozz.atlas.control.kafka.log.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.control.kafka.log.EventLogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Event Log 값 검색 조건")
public class EventLogSearchDto {

    // topic, eventType, aggregatePublicId, eventJson, lastError를 한 번에 검색하는 키워드
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // Kafka 토픽명으로 필터링
    @Schema(description = "topic 값", example = "sample", nullable = true)
    private String topic;

    // Kafka 이벤트 타입으로 필터링
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String eventType;

    // 이벤트 대상 도메인 종류로 필터링
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private AggregateType aggregateType;

    // 이벤트 대상 publicId로 필터링
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String aggregatePublicId;

    // 발행 결과 상태로 필터링, PUBLISHED 또는 FAILED
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private EventLogStatus status;
}
