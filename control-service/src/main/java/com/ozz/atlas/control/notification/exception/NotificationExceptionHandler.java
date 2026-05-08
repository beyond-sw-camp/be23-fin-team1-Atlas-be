package com.ozz.atlas.control.notification.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.control.notification.controller")
public class NotificationExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException e) {
        log.warn("NotificationException: {}", e.getMessage());
        return toErrorResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return badRequest("BAD_REQUEST", firstValidationMessage(e, "유효하지 않은 요청 값입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("UnhandledException: ", e);
        return internalServerError("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
    }
}
