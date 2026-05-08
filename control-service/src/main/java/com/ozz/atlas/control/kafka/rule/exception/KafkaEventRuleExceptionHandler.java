package com.ozz.atlas.control.kafka.rule.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.control.kafka.monitoring.controller")
public class KafkaEventRuleExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(KafkaEventRuleException.class)
    public ResponseEntity<ErrorResponse> handleKafkaEventRuleException(KafkaEventRuleException e) {
        log.warn("KafkaEventRuleException: {}", e.getMessage());
        return toErrorResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return badRequest("BAD_REQUEST", firstValidationMessage(e, "유효하지 않은 요청 값입니다."));
    }
}
