package com.ozz.atlas.supply.item.exception;

import com.ozz.atlas.common.exception.BaseException;

public class ItemException extends BaseException {

    public ItemException(ItemErrorCode errorCode) {
        super(errorCode);
    }
}
