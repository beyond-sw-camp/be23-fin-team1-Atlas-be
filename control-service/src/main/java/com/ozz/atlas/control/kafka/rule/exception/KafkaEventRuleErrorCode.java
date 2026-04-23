package com.ozz.atlas.control.kafka.rule.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaEventRuleErrorCode implements ErrorCode {
    RULE_NOT_FOUND(404, "KAFKA_RULE_001", "Kafka 이벤트 규칙을 찾을 수 없습니다."),
    DUPLICATE_RULE_CODE(409, "KAFKA_RULE_002", "이미 존재하는 규칙 코드입니다.");

    private final int status;
    private final String code;
    private final String message;
}
