package com.ozz.atlas.control.chat.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.control.chat.controller")
public class ChatExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(ChatException e) {
        log.warn("ChatException: {}", e.getMessage());
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
