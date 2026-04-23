package com.ozz.atlas.supply.lot.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LotErrorCode implements ErrorCode {
    LOT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "LOT_001", "로트 정보를 찾을 수 없습니다."),
    INVALID_LOT_QUANTITY(HttpStatus.BAD_REQUEST.value(), "LOT_002", "유효하지 않은 로트 수량입니다."),
    PO_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "LOT_003", "원본 발주 상세 정보를 찾을 수 없습니다."),
    DUPLICATE_LOT_NUMBER(HttpStatus.BAD_REQUEST.value(), "LOT_004", "이미 존재하는 LOT 번호입니다.");

    private final int status;
    private final String code;
    private final String message;
}