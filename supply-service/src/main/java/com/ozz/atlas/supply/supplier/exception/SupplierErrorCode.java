package com.ozz.atlas.supply.supplier.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND(404, "SUP_001", "해당 협력사를 찾을 수 없습니다."),
    LOGIN_SUPPLIER_NOT_FOUND(404, "SUP_002", "로그인한 조직에 연결된 협력사를 찾을 수 없습니다."),
    ACCESS_DENIED(403, "SUP_003", "해당 협력사에 접근할 권한이 없습니다."),
    INVALID_ACTOR_HEADER(400, "SUP_004", "사용자 또는 조직 헤더가 올바르지 않습니다."),
    SUPPLIER_CODE_ALREADY_EXISTS(409, "SUP_005", "이미 존재하는 협력사 코드입니다."),
    TIER_LIST_FORBIDDEN(403, "SUP_006", "tier별 조회는 발주사 조직 또는 관리자만 가능합니다."),
    SUPPLIER_SEARCH_FORBIDDEN(403, "SUP_007", "협력사 조직 사용자는 검색 조건 없이 다음 tier 협력사 목록만 조회할 수 있습니다."),
    INTERNAL_SERVER_ERROR(500, "SUP_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
