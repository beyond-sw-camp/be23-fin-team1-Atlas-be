package com.ozz.atlas.supply.returns.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.returns.controller")
public class ReturnExceptionHandler {

    @ExceptionHandler(ReturnException.class)
    public ResponseEntity<ErrorResponse> handleReturnException(ReturnException e) {
        log.warn("ReturnException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                e.getErrorCode().getStatus(),
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                400,
                "BAD_REQUEST",
                e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("UnhandledException: ", e);
        ErrorResponse response = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.internalServerError().body(response);
    }
}