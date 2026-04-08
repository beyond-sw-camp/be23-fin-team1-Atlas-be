package com.ozz.atlas.supply.subpurchaseorder.exception;

import com.ozz.atlas.common.exception.BaseException;

public class SubPurchaseOrderException extends BaseException {

    public SubPurchaseOrderException(SubPurchaseOrderErrorCode errorCode) {
        super(errorCode);
    }
}
