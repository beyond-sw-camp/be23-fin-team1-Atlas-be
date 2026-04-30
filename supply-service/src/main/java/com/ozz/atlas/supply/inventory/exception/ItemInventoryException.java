package com.ozz.atlas.supply.inventory.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class ItemInventoryException extends BaseException {

    public ItemInventoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
