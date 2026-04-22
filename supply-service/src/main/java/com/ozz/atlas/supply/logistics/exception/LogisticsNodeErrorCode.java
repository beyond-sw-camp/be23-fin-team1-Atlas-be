package com.ozz.atlas.supply.logistics.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogisticsNodeErrorCode implements ErrorCode {

    INVALID_INPUT_VALUE(400, "LOGISTICS_000", "요청 값이 올바르지 않습니다."),
    NODE_CODE_ALREADY_EXISTS(400, "LOGISTICS_001", "이미 사용 중인 물류거점 코드입니다."),
    NODE_NOT_FOUND(404, "LOGISTICS_002", "물류거점을 찾을 수 없습니다."),
    ADDRESS_GEOCODING_FAILED(400, "LOGISTICS_003", "주소를 좌표로 변환할 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "LOGISTICS_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
