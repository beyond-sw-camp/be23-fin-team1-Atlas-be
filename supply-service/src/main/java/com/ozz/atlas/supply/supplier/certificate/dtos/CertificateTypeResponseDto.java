package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateTypeResponseDto {
    private String publicId;
    private String certificateCode;
    private String certificateName;
    private CertificateScope scopeType;
    private String issuerName;
    private boolean requiredYn;
    private boolean activeYn;

    public static CertificateTypeResponseDto from(CertificateType entity) {
        return CertificateTypeResponseDto.builder()
                .publicId(entity.getPublicId())
                .certificateCode(entity.getCertificateCode())
                .certificateName(entity.getCertificateName())
                .scopeType(entity.getScopeType())
                .issuerName(entity.getIssuerName())
                .requiredYn(entity.isRequiredYn())
                .activeYn(entity.isActiveYn())
                .build();
    }
}