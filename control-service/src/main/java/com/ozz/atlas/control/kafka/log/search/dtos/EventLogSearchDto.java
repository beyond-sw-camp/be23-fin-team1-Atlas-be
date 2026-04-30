package com.ozz.atlas.control.kafka.log.search.dtos;

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
public class EventLogSearchDto {

    // topic, eventType, aggregatePublicId, eventJson, lastError를 한 번에 검색하는 키워드
    private String keyword;

    // Kafka 토픽명으로 필터링
    private String topic;

    // Kafka 이벤트 타입으로 필터링
    private String eventType;

    // 이벤트 대상 도메인 종류로 필터링
    private AggregateType aggregateType;

    // 이벤트 대상 publicId로 필터링
    private String aggregatePublicId;

    // 발행 결과 상태로 필터링, PUBLISHED 또는 FAILED
    private EventLogStatus status;
}
