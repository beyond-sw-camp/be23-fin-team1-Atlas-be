package com.ozz.atlas.supply.supplier.relation.exception;

import com.ozz.atlas.common.exception.BaseException;

public class SupplierRelationException extends BaseException {

    public SupplierRelationException(SupplierRelationErrorCode errorCode) {
        super(errorCode);
    }
}
