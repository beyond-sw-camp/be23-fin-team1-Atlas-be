package com.ozz.atlas.supply.inventory.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemInventoryErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND(404, "INV_001", "로그인한 조직에 연결된 협력사를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(404, "INV_002", "품목을 찾을 수 없습니다."),
    INVENTORY_NOT_FOUND(404, "INV_003", "재고를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(400, "INV_004", "유효하지 않은 재고 요청입니다."),
    INVENTORY_EDIT_NOT_ALLOWED(409, "INV_005", "예약된 재고는 수정할 수 없습니다."),
    INVENTORY_DELETE_NOT_ALLOWED(409, "INV_006", "예약된 재고는 삭제/폐기할 수 없습니다."),
    INVENTORY_INSUFFICIENT(409, "INV_007", "주문 가능한 재고 수량이 부족합니다."),
    LOGISTICS_NODE_NOT_FOUND(404, "INV_008", "창고를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
