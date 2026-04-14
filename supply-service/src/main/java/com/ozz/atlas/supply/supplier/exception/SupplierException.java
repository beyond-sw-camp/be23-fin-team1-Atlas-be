package com.ozz.atlas.supply.supplier.exception;

import com.ozz.atlas.common.exception.BaseException;

public class SupplierException extends BaseException {

    public SupplierException(SupplierErrorCode errorCode) {
        super(errorCode);
    }
}
