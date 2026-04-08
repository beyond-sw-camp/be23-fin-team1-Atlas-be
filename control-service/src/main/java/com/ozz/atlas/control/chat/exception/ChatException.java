package com.ozz.atlas.control.chat.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class ChatException extends BaseException {
    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}