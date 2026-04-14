package com.ozz.atlas.supply.item.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(404, "ITEM_001", "해당 품목이 존재하지 않습니다."),
    ITEM_CODE_ALREADY_EXISTS(409, "ITEM_002", "이미 존재하는 품목 코드입니다."),
    ITEM_CATEGORY_NOT_FOUND(404, "ITEM_003", "해당 품목 카테고리가 존재하지 않습니다."),
    CATEGORY_NOT_FOUND(404, "ITEM_004", "해당 카테고리가 존재하지 않습니다."),
    PARENT_CATEGORY_NOT_FOUND(404, "ITEM_005", "해당 상위 카테고리가 존재하지 않습니다."),
    CATEGORY_CHILD_EXISTS(409, "ITEM_006", "하위 카테고리가 있어 수정 또는 삭제할 수 없습니다."),
    CATEGORY_SELF_PARENT(400, "ITEM_007", "자기 자신을 상위 카테고리로 설정할 수 없습니다."),
    ITEM_EXISTS_IN_CATEGORY(409, "ITEM_008", "해당 카테고리에 품목이 있어 삭제할 수 없습니다."),
    INVALID_INPUT_VALUE(400, "ITEM_009", "유효하지 않은 요청 값입니다."),
    CATEGORY_WRITE_FORBIDDEN(403, "ITEM_010", "품목 카테고리는 관리자 또는 발주사 조직만 등록/수정/삭제할 수 있습니다."),
    CATEGORY_OWNER_FORBIDDEN(403, "ITEM_011", "다른 조직이 만든 카테고리는 수정 또는 삭제할 수 없습니다."),
    ACCESS_DENIED(403, "ITEM_012", "해당 리소스에 접근할 권한이 없습니다."),
    INVALID_ACTOR_HEADER(400, "ITEM_013", "사용자 또는 조직 헤더가 올바르지 않습니다."),
    INVALID_ORGANIZATION_TYPE(403, "ITEM_014", "허용되지 않은 조직 타입입니다."),
    SUPPLIER_NOT_FOUND(404, "ITEM_015", "로그인한 조직에 연결된 협력사를 찾을 수 없습니다."),
    SUPPLIER_NOT_APPROVED(403, "ITEM_016", "승인된 협력사만 품목을 관리할 수 있습니다."),
    SUPPLIER_NOT_ACTIVE(403, "ITEM_017", "활성 상태의 협력사만 품목을 관리할 수 있습니다."),
    INTERNAL_SERVER_ERROR(500, "ITEM_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
