package com.ozz.atlas.supply.supplier.certificate.exception;

import com.ozz.atlas.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CertificateErrorCode implements ErrorCode {
    CERTIFICATE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "CERT_001", "인증 유형을 찾을 수 없습니다."),
    CERTIFICATE_TYPE_DUPLICATED(HttpStatus.BAD_REQUEST.value(), "CERT_002", "이미 존재하는 인증 코드입니다."),
    SUPPLIER_CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "CERT_003", "협력사 인증서를 찾을 수 없습니다."),
    INVALID_CERTIFICATE_DATES(HttpStatus.BAD_REQUEST.value(), "CERT_004", "만료일은 발급일 이후여야 합니다.");

    private final int status;
    private final String code;
    private final String message;
}