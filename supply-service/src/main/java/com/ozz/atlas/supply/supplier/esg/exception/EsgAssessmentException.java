package com.ozz.atlas.supply.supplier.esg.exception;

import com.ozz.atlas.common.exception.BaseException;

public class EsgAssessmentException extends BaseException {

    public EsgAssessmentException(EsgAssessmentErrorCode errorCode) {
        super(errorCode);
    }
}
