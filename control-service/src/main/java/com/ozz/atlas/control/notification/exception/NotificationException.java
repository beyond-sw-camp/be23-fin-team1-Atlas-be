package com.ozz.atlas.control.notification.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class NotificationException extends BaseException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}