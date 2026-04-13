package com.ozz.atlas.supply.settlement.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class SettlementException extends BaseException {

    public SettlementException(ErrorCode errorCode){
        super(errorCode);
    }
}
