package com.ozz.atlas.control.kafka.rule.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.control.kafka.monitoring.controller")
public class KafkaEventRuleExceptionHandler {

    @ExceptionHandler(KafkaEventRuleException.class)
    public ResponseEntity<ErrorResponse> handleKafkaEventRuleException(KafkaEventRuleException e) {
        log.warn("KafkaEventRuleException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                e.getErrorCode().getStatus(),
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("ValidationException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                400,
                "BAD_REQUEST",
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }
}
