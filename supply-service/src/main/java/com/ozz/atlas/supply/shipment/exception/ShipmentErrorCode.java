package com.ozz.atlas.supply.shipment.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentErrorCode implements ErrorCode {

    INVALID_INPUT_VALUE(400, "SHIPMENT_000", "요청 값이 올바르지 않습니다."),
    INVALID_TRACK_REQUEST(400, "SHIPMENT_001", "출하 체크포인트 요청이 올바르지 않습니다."),
    SHIPMENT_NOT_FOUND(404, "SHIPMENT_002", "출하 정보를 찾을 수 없습니다."),
    LOT_NOT_FOUND(404, "SHIPMENT_003", "LOT 정보를 찾을 수 없습니다."),
    INVALID_SHIPMENT_LOT_MAPPING_REQUEST(400, "SHIPMENT_004", "출하 LOT 매핑 요청이 올바르지 않습니다."),
    DUPLICATE_SHIPMENT_LOT_MAPPING(409, "SHIPMENT_005", "이미 해당 출하에 매핑된 LOT입니다."),
    INVALID_SHIPMENT_STATUS_TRANSITION(400, "SHIPMENT_006", "출하 상태 변경 요청이 올바르지 않습니다."),
    ACCESS_DENIED(403, "SHIPMENT_007", "출하 정보에 접근할 권한이 없습니다."),
    INACTIVE_LOGISTICS_NODE(400, "SHIPMENT_008", "비활성 물류거점은 출하에 사용할 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "SHIPMENT_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
