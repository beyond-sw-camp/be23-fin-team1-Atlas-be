package com.ozz.atlas.supply.returns.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReturnErrorCode implements ErrorCode {
    RETURN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "RTN_001", "반품 요청 정보를 찾을 수 없습니다."),
    INVALID_RETURN_QUANTITY(HttpStatus.BAD_REQUEST.value(), "RTN_002", "반품 수량은 원본 발주/출하 수량을 초과할 수 없습니다."),
    INVALID_RETURN_STATUS_TRANSITION(HttpStatus.BAD_REQUEST.value(), "RTN_003", "유효하지 않은 반품 상태 변경입니다.");

    private final int status;
    private final String code;
    private final String message;
}