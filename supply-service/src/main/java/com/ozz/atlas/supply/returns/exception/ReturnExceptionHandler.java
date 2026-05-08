package com.ozz.atlas.supply.returns.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.returns.controller")
public class ReturnExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(ReturnException.class)
    public ResponseEntity<ErrorResponse> handleReturnException(ReturnException e) {
        log.warn("ReturnException: {}", e.getMessage());
        return toErrorResponse(e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        return badRequest("BAD_REQUEST", e.getMessage());
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
