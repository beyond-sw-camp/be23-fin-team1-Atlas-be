package com.ozz.atlas.common.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "서비스 간 Kafka 이벤트 전달에 사용하는 공통 envelope")
public record EventEnvelope<T>(
        @Schema(description = "이벤트 고유 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KA")
        String eventId,
        @Schema(description = "세부 이벤트 유형", example = "shipment.delay-detected")
        String eventType,
        @Schema(description = "이벤트 스키마 버전", example = "v1")
        String schemaVersion,
        @Schema(description = "이벤트 발행 서비스명", example = "supply-service")
        String producer,
        @Schema(description = "발행 대상 Kafka 토픽", example = "atlas.supply.shipment")
        String topic,
        @Schema(description = "이벤트의 aggregate 유형")
        AggregateType aggregateType,
        @Schema(description = "aggregate 공개 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KB")
        String aggregatePublicId,
        @Schema(description = "파티션 키", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KB")
        String partitionKey,
        @Schema(description = "이벤트 발생 시각", example = "2026-04-20T07:00:00Z")
        Instant occurredAt,
        @Schema(description = "상위 요청 또는 흐름을 묶는 상관관계 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KC", nullable = true)
        String correlationId,
        @Schema(description = "이벤트 발생 원인이 된 선행 이벤트 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KD", nullable = true)
        String causationId,
        @Schema(description = "이벤트를 발생시킨 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0", nullable = true)
        String actorUserPublicId,
        @Schema(description = "이벤트와 연관된 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4", nullable = true)
        String organizationPublicId,
        @Schema(description = "도메인별 상세 payload")
        T payload
) {
    // topic과 eventType은 역할이 다름
    // topic은 큰 도메인 분류, eventType은 실제 상태 변화나 행위
}
