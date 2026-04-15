package com.ozz.atlas.supply.batch.exception;

import com.ozz.atlas.common.exception.BaseException;

public class BatchException extends BaseException {

    public BatchException(BatchErrorCode errorCode) {
        super(errorCode);
    }
}
