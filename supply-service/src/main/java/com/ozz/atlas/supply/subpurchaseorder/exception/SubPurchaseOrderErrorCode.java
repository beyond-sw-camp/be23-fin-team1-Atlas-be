package com.ozz.atlas.supply.subpurchaseorder.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubPurchaseOrderErrorCode implements ErrorCode {

    PARENT_PURCHASE_ORDER_NOT_FOUND(404, "SUB_PO_001", "상위 발주를 찾을 수 없습니다."),
    TARGET_SUPPLIER_NOT_FOUND(404, "SUB_PO_002", "서브발주 대상 협력사를 찾을 수 없습니다."),
    SUB_PURCHASE_ORDER_NOT_FOUND(404, "SUB_PO_003", "서브발주를 찾을 수 없습니다."),
    SUB_PURCHASE_ORDER_ACCESS_DENIED(403, "SUB_PO_004", "해당 서브발주에 접근할 수 없습니다."),
    SUB_PURCHASE_ORDER_NUMBER_ALREADY_EXISTS(409, "SUB_PO_005", "이미 존재하는 서브발주 번호입니다."),
    SUB_PURCHASE_ORDER_DUPLICATE_PARENT_ITEM(409, "SUB_PO_006", "같은 부모 발주 품목에 같은 하위 품목을 중복 등록할 수 없습니다."),
    SUB_PURCHASE_ORDER_ITEM_NOT_FOUND(404, "SUB_PO_007", "부모 발주 품목 또는 하위 협력사 품목을 찾을 수 없습니다."),
    SUB_PURCHASE_ORDER_QTY_EXCEEDED(409, "SUB_PO_008", "허용 가능한 서브발주 수량을 초과했습니다."),
    SUB_PURCHASE_ORDER_STATUS_NOT_ALLOWED(409, "SUB_PO_009", "현재 상태에서는 서브발주를 처리할 수 없습니다."),
    SUB_PURCHASE_ORDER_SAME_SUPPLIER_NOT_ALLOWED(409, "SUB_PO_010", "자기 자신에게 서브발주할 수 없습니다."),
    SUB_PURCHASE_ORDER_CONFIRM_QTY_INVALID(400, "SUB_PO_011", "확정 수량은 발주 수량을 초과할 수 없습니다."),
    INVALID_INPUT_VALUE(400, "SUB_PO_012", "유효하지 않은 요청 값입니다."),

    TARGET_SUPPLIER_CAPABILITY_NOT_FOUND(404, "SUB_PO_013", "대상 협력사의 품목 공급 역량을 찾을 수 없습니다."),
    SUB_PURCHASE_ORDER_MOQ_NOT_MET(409, "SUB_PO_014", "서브발주 수량이 최소 주문 수량보다 작습니다."),
    SUB_PURCHASE_ORDER_AVAILABLE_QTY_EXCEEDED(409, "SUB_PO_015", "서브발주 수량이 현재 공급 가능 수량을 초과했습니다."),
    SUB_PURCHASE_ORDER_MONTHLY_CAPACITY_EXCEEDED(409, "SUB_PO_016", "서브발주 수량이 월 생산 가능 수량을 초과했습니다."),
    SUB_PURCHASE_ORDER_LEAD_TIME_NOT_MET(409, "SUB_PO_017", "리드타임 기준으로 요청 납기를 맞출 수 없습니다."),
    SUB_PURCHASE_ORDER_CAPABILITY_NOT_ACTIVE(409, "SUB_PO_018", "해당 품목 공급 역량의 유효 시작일 이전에는 서브발주할 수 없습니다."),
    TARGET_SUPPLIER_TIER_NOT_ALLOWED(409, "SUB_PO_019", "서브발주 대상 협력사는 발행 협력사보다 하위 tier여야 합니다."),
    SUB_PURCHASE_ORDER_ITEM_SUPPLIER_MISMATCH(409, "SUB_PO_020", "선택한 품목은 대상 협력사 소속 품목이어야 합니다."),

    INTERNAL_SERVER_ERROR(500, "SUB_PO_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
