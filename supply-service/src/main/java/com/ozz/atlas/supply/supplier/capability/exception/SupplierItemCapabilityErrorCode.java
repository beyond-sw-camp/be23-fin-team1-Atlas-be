package com.ozz.atlas.supply.supplier.capability.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierItemCapabilityErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND(404, "CAP_001", "해당 협력사가 존재하지 않습니다."),
    ITEM_NOT_FOUND(404, "CAP_002", "해당 품목이 존재하지 않습니다."),
    CAPABILITY_ALREADY_EXISTS(409, "CAP_003", "이미 등록된 협력사 품목 공급 역량입니다."),
    CAPABILITY_NOT_FOUND(404, "CAP_004", "해당 협력사 품목 공급 역량이 존재하지 않습니다."),
    EMPTY_PATCH_NOT_ALLOWED(400, "CAP_005", "수정할 공급 역량 항목이 없습니다."),
    INVALID_INPUT_VALUE(400, "CAP_006", "유효하지 않은 요청 값입니다."),
    INTERNAL_SERVER_ERROR(500, "CAP_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
