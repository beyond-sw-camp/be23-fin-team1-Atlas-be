package com.ozz.atlas.supply.subpurchaseorder.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.subpurchaseorder")
public class SubPurchaseOrderExceptionHandler {

    @ExceptionHandler(SubPurchaseOrderException.class)
    public ResponseEntity<ErrorResponse> handleSubPurchaseOrderException(SubPurchaseOrderException e) {
        SubPurchaseOrderErrorCode errorCode = (SubPurchaseOrderErrorCode) e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        SubPurchaseOrderErrorCode errorCode = SubPurchaseOrderErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        SubPurchaseOrderErrorCode errorCode = SubPurchaseOrderErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }
}
