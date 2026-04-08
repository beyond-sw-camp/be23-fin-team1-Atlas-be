package com.ozz.atlas.supply.supplier.esg.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.supplier.esg")
public class EsgAssessmentExceptionHandler {

    @ExceptionHandler(EsgAssessmentException.class)
    public ResponseEntity<ErrorResponse> handleEsgAssessmentException(EsgAssessmentException e) {
        EsgAssessmentErrorCode errorCode = (EsgAssessmentErrorCode) e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        EsgAssessmentErrorCode errorCode = EsgAssessmentErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        EsgAssessmentErrorCode errorCode = EsgAssessmentErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }
}
