package com.ozz.atlas.supply.settlement.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SettlementErrorCode implements ErrorCode {

    INVALID_SETTLEMENT_REQUEST(400, "SETTLEMENT_000", "정산 요청 값이 올바르지 않습니다."),
    INVALID_ACTOR_HEADER(400, "SETTLEMENT_007", "사용자 헤더 값이 올바르지 않습니다."),
    SETTLEMENT_NOT_FOUND(404, "SETTLEMENT_001", "정산 정보를 찾을 수 없습니다."),
    SETTLEMENT_DETAIL_NOT_FOUND(404, "SETTLEMENT_002", "정산 상세 정보를 찾을 수 없습니다."),
    INVALID_SETTLEMENT_STATUS_TRANSITION(400, "SETTLEMENT_003", "정산 상태 변경 요청이 올바르지 않습니다."),
    SUPPLIER_NOT_FOUND(404, "SETTLEMENT_004", "협력사 정보를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "SETTLEMENT_999", "서버 내부 오류가 발생했습니다."),
    FORBIDDEN_SETTLEMENT_APPROVAL(403, "SETTLEMENT_005", "정산 승인 권한이 없습니다."),
    FORBIDDEN_SETTLEMENT_CANCEL(403, "SETTLEMENT_006", "정산 취소 권한이 없습니다."),
    INVALID_SHIPMENT_STATUS_TRANSITION(400, "SHIPMENT_006", "출하 상태 변경 요청이 올바르지 않습니다."),
    RETURN_NOT_FOUND(404, "SETTLEMENT_008", "반품 정보를 찾을 수 없습니다."),
    RETURN_NOT_SETTLABLE(400, "SETTLEMENT_009", "정산 반영 가능한 반품 상태가 아닙니다."),
    DUPLICATE_SETTLEMENT_TARGET(400, "SETTLEMENT_010", "이미 정산에 반영된 대상입니다.");

    private final int status;
    private final String code;
    private final String message;
}
