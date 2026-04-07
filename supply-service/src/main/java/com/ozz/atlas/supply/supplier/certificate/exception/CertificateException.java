package com.ozz.atlas.supply.supplier.certificate.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;

public class CertificateException extends BaseException {
    public CertificateException(ErrorCode errorCode) {
        super(errorCode);
    }
}