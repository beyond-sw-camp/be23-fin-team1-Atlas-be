package com.ozz.atlas.supply.supplier.esg.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EsgAssessmentErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND(404, "ESG_001", "해당 협력사가 존재하지 않습니다."),
    ESG_ASSESSMENT_NOT_FOUND(404, "ESG_002", "해당 ESG 평가가 존재하지 않습니다."),
    ESG_ASSESSMENT_EMPTY_PATCH(400, "ESG_003", "수정할 ESG 평가 항목이 없습니다."),
    INVALID_INPUT_VALUE(400, "ESG_004", "유효하지 않은 요청 값입니다."),
    INTERNAL_SERVER_ERROR(500, "ESG_999", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
