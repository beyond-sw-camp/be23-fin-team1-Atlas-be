package com.ozz.atlas.supply.batch.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BatchErrorCode implements ErrorCode {

    BATCH_ALREADY_RUNNING(409, "BATCH_409_001", "다른 배치가 실행 중입니다. 잠시 후 다시 시도해 주세요."),
    BATCH_EXECUTION_FAILED(500, "BATCH_500_001", "배치 실행 중 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
