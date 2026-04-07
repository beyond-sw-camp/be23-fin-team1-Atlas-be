package com.ozz.atlas.supply.purchaseorder.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.purchaseorder")
public class PurchaseOrderExceptionHandler {

    @ExceptionHandler(PurchaseOrderException.class)
    public ResponseEntity<ErrorResponse> handlePurchaseOrderException(PurchaseOrderException e) {
        PurchaseOrderErrorCode errorCode = (PurchaseOrderErrorCode) e.getErrorCode();

        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        PurchaseOrderErrorCode errorCode = PurchaseOrderErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        PurchaseOrderErrorCode errorCode = PurchaseOrderErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
