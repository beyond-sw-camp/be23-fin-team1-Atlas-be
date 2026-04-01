package com.ozz.atlas.supply.shipment.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentErrorCode implements ErrorCode {

    INVALID_INPUT_VALUE(400, "SHIPMENT_000", "요청 값이 올바르지 않습니다."),
    SHIPMENT_NOT_FOUND(404, "SHIPMENT_001", "출하 정보를 찾을 수 없습니다."),
    INVALID_TRACK_REQUEST(400, "SHIPMENT_002", "출하 체크포인트 요청이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(500, "SHIPMENT_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
