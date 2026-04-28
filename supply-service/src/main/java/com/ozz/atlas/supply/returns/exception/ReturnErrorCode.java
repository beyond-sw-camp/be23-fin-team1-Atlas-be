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
    INVALID_RETURN_REQUEST(HttpStatus.BAD_REQUEST.value(), "RTN_004", "반품 요청 값이 올바르지 않습니다."),
    FORBIDDEN_RETURN_CREATE(HttpStatus.FORBIDDEN.value(), "RTN_005", "발주사 조직만 반품을 생성할 수 있습니다."),
    INVALID_RETURN_STATUS_TRANSITION(HttpStatus.BAD_REQUEST.value(), "RTN_003", "유효하지 않은 반품 상태 변경입니다."),
    INVALID_RETURN_SOURCE_SHIPMENT(HttpStatus.BAD_REQUEST.value(), "RTN_006", "도착 완료된 출하건만 반품 요청할 수 있습니다."),
    DUPLICATE_RETURN_REQUEST(HttpStatus.CONFLICT.value(), "RTN_007", "이미 반품 요청이 생성된 출하건입니다.");

    private final int status;
    private final String code;
    private final String message;
}