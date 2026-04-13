package com.ozz.atlas.supply.onboarding.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements ErrorCode {

    SUPPLIER_CODE_ALREADY_EXISTS(409, "ONB_001", "이미 존재하는 협력사 코드입니다."),
    REQUEST_NOT_FOUND(404, "ONB_002", "해당 협력사 등록 요청을 찾을 수 없습니다."),
    REVIEWABLE_REQUEST_NOT_FOUND(404, "ONB_003", "승인 또는 반려할 수 있는 협력사 등록 요청을 찾을 수 없습니다."),
    ACCESS_DENIED(403, "ONB_004", "해당 협력사 등록 요청에 접근할 수 없습니다."),
    ADMIN_ONLY_ACTION(403, "ONB_005", "관리자만 협력사 등록 요청을 승인 또는 반려할 수 있습니다."),
    INVALID_ACTOR_HEADER(400, "ONB_006", "요청자 정보 헤더가 올바르지 않습니다.");

    private final int status;
    private final String code;
    private final String message;
}
