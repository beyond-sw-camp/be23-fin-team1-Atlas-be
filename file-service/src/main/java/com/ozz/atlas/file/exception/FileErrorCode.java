package com.ozz.atlas.file.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
    INVALID_FILE_TYPE(400, "FILE_001", "지원하지 않는 파일 형식입니다."),
    INVALID_FILE_REQUEST(400, "FILE_002", "잘못된 파일 요청입니다."),
    FILE_NOT_FOUND(404, "FILE_003", "파일을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND(404, "FILE_004", "첨부 정보를 찾을 수 없습니다."),
    ATTACHMENT_ALREADY_EXISTS(409, "FILE_005", "이미 첨부 정보가 존재합니다."),
    FILE_SIZE_EXCEEDED(413, "FILE_006", "업로드 가능한 파일 크기를 초과했습니다."),
    FILE_UPLOAD_FAILED(500, "FILE_007", "파일 업로드에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
