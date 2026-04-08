package com.ozz.atlas.supply.supplier.capability.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.supplier.capability")
public class SupplierItemCapabilityExceptionHandler {

    @ExceptionHandler(SupplierItemCapabilityException.class)
    public ResponseEntity<ErrorResponse> handleCapabilityException(SupplierItemCapabilityException e) {
        SupplierItemCapabilityErrorCode errorCode = (SupplierItemCapabilityErrorCode) e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        SupplierItemCapabilityErrorCode errorCode = SupplierItemCapabilityErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        SupplierItemCapabilityErrorCode errorCode = SupplierItemCapabilityErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }
}
