package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCertificateTypeRequestDto {
    private String certificateName;
    private CertificateScope scopeType;
    private String issuerName;
    private Boolean requiredYn;
    private Boolean activeYn;
}