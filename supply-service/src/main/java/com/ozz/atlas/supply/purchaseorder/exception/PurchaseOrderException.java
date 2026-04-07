package com.ozz.atlas.supply.purchaseorder.exception;

import com.ozz.atlas.common.exception.BaseException;

public class PurchaseOrderException extends BaseException {

    public PurchaseOrderException(PurchaseOrderErrorCode errorCode) {
        super(errorCode);
    }
}
