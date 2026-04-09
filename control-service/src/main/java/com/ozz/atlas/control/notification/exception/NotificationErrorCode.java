package com.ozz.atlas.control.notification.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND(404, "NOTIF_001", "알림을 찾을 수 없습니다."),
    NOT_NOTIFICATION_OWNER(403, "NOTIF_002", "본인의 알림만 접근할 수 있습니다.");

    private final int status;
    private final String code;
    private final String message;
}