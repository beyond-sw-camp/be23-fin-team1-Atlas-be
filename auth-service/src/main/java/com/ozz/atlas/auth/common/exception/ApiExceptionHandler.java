package com.ozz.atlas.auth.common.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler extends BaseExceptionAdviceSupport {

    // 로그인 실패는 프론트가 바로 읽을 수 있는 JSON 으로
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailed(LoginFailedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getFailureReason(), e.getMessage())
        );
    }

    // 기존 IllegalArgumentException 도 JSON 으로
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return badRequest("BAD_REQUEST", e.getMessage());
    }
}
