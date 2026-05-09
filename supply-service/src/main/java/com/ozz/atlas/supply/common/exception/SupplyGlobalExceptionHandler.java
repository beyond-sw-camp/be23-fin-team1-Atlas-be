package com.ozz.atlas.supply.common.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ozz.atlas.supply")
public class SupplyGlobalExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        return toErrorResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return badRequest("COMMON_400", firstValidationMessage(e, "유효하지 않은 요청 값입니다."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return badRequest("COMMON_400", "유효하지 않은 요청 값입니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();

        return internalServerError("COMMON_500", "서버 내부 오류가 발생했습니다.");
    }
}
