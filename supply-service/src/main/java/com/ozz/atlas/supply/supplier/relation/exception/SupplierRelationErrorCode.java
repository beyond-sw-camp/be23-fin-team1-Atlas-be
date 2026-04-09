package com.ozz.atlas.supply.supplier.relation.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierRelationErrorCode implements ErrorCode {

    PARENT_SUPPLIER_NOT_FOUND(404, "REL_001", "상위 협력사를 찾을 수 없습니다."),
    CHILD_SUPPLIER_NOT_FOUND(404, "REL_002", "하위 협력사를 찾을 수 없습니다."),
    RELATION_NOT_FOUND(404, "REL_003", "협력사 연결관계를 찾을 수 없습니다."),
    RELATION_ALREADY_EXISTS(409, "REL_004", "이미 등록된 협력사 연결관계입니다."),
    SELF_RELATION_NOT_ALLOWED(400, "REL_005", "동일 협력사 간 연결관계는 등록할 수 없습니다."),
    INVALID_DATE_RANGE(400, "REL_006", "유효 시작일과 종료일 범위가 올바르지 않습니다."),
    ACCESS_DENIED(403, "REL_007", "해당 협력사 연결관계에 접근할 수 없습니다."),
    INVALID_INPUT_VALUE(400, "REL_008", "유효하지 않은 요청 값입니다."),
    INTERNAL_SERVER_ERROR(500, "REL_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
