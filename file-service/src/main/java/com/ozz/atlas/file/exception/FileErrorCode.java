package com.ozz.atlas.file.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
    INVALID_FILE_TYPE(400, "FILE_001", "지원하지 않는 파일 형식입니다."),
    FILE_NOT_FOUND(404, "FILE_002", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(500, "FILE_003", "파일 업로드에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
