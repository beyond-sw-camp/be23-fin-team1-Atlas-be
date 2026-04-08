package com.ozz.atlas.control.chat.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHAT_ROOM_NOT_FOUND(404, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(404, "CHAT_002", "메시지를 찾을 수 없습니다."),
    NOT_MESSAGE_SENDER(403, "CHAT_003", "본인이 작성한 메시지만 수정/삭제할 수 있습니다."),
    MESSAGE_ALREADY_DELETED(400, "CHAT_004", "삭제된 메시지는 수정할 수 없습니다."),
    INVALID_REFERENCE_DATA(400, "CHAT_005", "유효하지 않은 참조 데이터입니다.");

    private final int status;
    private final String code;
    private final String message;
}