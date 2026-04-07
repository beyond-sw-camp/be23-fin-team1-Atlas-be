package com.ozz.atlas.supply.purchaseorder.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseOrderErrorCode implements ErrorCode {

    PURCHASE_ORDER_NOT_FOUND(404, "PO_001", "해당 발주가 존재하지 않습니다."),
    PURCHASE_ORDER_NUMBER_ALREADY_EXISTS(409, "PO_002", "이미 존재하는 발주 번호입니다."),
    PURCHASE_ORDER_SUPPLIER_NOT_FOUND(404, "PO_003", "발주 가능한 협력사가 존재하지 않습니다."),
    PURCHASE_ORDER_ITEM_NOT_FOUND(404, "PO_004", "발주 대상 품목이 존재하지 않습니다."),
    PURCHASE_ORDER_ITEM_EMPTY(400, "PO_005", "발주 상세는 최소 1건 이상이어야 합니다."),
    PURCHASE_ORDER_DUPLICATE_ITEM(400, "PO_006", "동일한 품목은 한 발주에 중복으로 등록할 수 없습니다."),
    INVALID_INPUT_VALUE(400, "PO_007", "유효하지 않은 요청 값입니다."),
    INTERNAL_SERVER_ERROR(500, "PO_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
