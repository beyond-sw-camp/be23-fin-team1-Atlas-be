package com.ozz.atlas.supply.returns.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class ReturnException extends BaseException {
    public ReturnException(ErrorCode errorCode) {
        super(errorCode);
    }
}