package com.ozz.atlas.supply.supplier.capability.exception;

import com.ozz.atlas.common.exception.BaseException;

public class SupplierItemCapabilityException extends BaseException {

    public SupplierItemCapabilityException(SupplierItemCapabilityErrorCode errorCode) {
        super(errorCode);
    }
}
