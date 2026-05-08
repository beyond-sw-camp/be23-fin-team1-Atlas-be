package com.ozz.atlas.common.web.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;
import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

public abstract class BaseExceptionAdviceSupport {

    protected ResponseEntity<ErrorResponse> toErrorResponse(BaseException exception) {
        return toErrorResponse(exception.getErrorCode());
    }

    protected ResponseEntity<ErrorResponse> toErrorResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorResponse(errorCode));
    }

    protected ResponseEntity<ErrorResponse> badRequest(String code, String message) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, code, message));
    }

    protected ResponseEntity<ErrorResponse> internalServerError(String code, String message) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(500, code, message));
    }

    protected String firstValidationMessage(MethodArgumentNotValidException exception, String fallbackMessage) {
        return exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : fallbackMessage)
                .orElse(fallbackMessage);
    }

    protected ErrorResponse errorResponse(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
