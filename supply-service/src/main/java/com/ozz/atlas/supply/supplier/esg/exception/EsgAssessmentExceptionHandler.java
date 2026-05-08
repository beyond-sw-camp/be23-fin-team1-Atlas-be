package com.ozz.atlas.supply.supplier.esg.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.supplier.esg")
public class EsgAssessmentExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(EsgAssessmentException.class)
    public ResponseEntity<ErrorResponse> handleEsgAssessmentException(EsgAssessmentException e) {
        EsgAssessmentErrorCode errorCode = (EsgAssessmentErrorCode) e.getErrorCode();
        return toErrorResponse(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        EsgAssessmentErrorCode errorCode = EsgAssessmentErrorCode.INVALID_INPUT_VALUE;
        return toErrorResponse(errorCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        EsgAssessmentErrorCode errorCode = EsgAssessmentErrorCode.INTERNAL_SERVER_ERROR;
        return toErrorResponse(errorCode);
    }
}
