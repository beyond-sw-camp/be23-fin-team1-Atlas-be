package com.ozz.atlas.control.kafka.rule.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class KafkaEventRuleException extends BaseException {

    public KafkaEventRuleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
