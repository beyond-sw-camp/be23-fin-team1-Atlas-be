package com.ozz.atlas.supply.shipment.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply.shipment")
public class ShipmentExceptionHandler {

    @ExceptionHandler(ShipmentException.class)
    public ResponseEntity<ErrorResponse> handleShipmentException(ShipmentException e){
        ShipmentErrorCode errorCode = (ShipmentErrorCode) e.getErrorCode();
        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){
        ShipmentErrorCode errorCode = ShipmentErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e){
        ShipmentErrorCode errorCode = ShipmentErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
