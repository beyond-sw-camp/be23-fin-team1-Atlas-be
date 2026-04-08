package com.ozz.atlas.supply.lot.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class LotException extends BaseException {
    public LotException(ErrorCode errorCode) {
        super(errorCode);
    }
}