package com.ozz.atlas.auth.common.exception;

import com.ozz.atlas.auth.dtos.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 로그인 실패는 프론트가 바로 읽을 수 있는 JSON 으로
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleLoginFailed(LoginFailedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDto.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .code(e.getFailureReason())
                        .message(e.getMessage())
                        .build()
        );
    }

    // 기존 IllegalArgumentException 도 JSON 으로
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                ErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("BAD_REQUEST")
                        .message(e.getMessage())
                        .build()
        );
    }
}
