package com.ozz.atlas.supply.logistics.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class LogisticsNodeException extends BaseException {

    public LogisticsNodeException(ErrorCode errorCode){
        super(errorCode);
    }
}
