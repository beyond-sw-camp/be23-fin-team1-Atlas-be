package com.ozz.atlas.supply.shipment.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class ShipmentException extends BaseException {

    public ShipmentException(ErrorCode errorCode){
        super(errorCode);
    }
}
