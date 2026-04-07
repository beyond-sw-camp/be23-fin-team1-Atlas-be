package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateTypeResponseDto {
    private Long id;
    private String certificateCode;
    private String certificateName;
    private CertificateScope scopeType;
    private boolean requiredYn;
    private boolean activeYn;

    public static CertificateTypeResponseDto from(CertificateType entity) {
        return CertificateTypeResponseDto.builder()
                .id(entity.getId())
                .certificateCode(entity.getCertificateCode())
                .certificateName(entity.getCertificateName())
                .scopeType(entity.getScopeType())
                .requiredYn(entity.isRequiredYn())
                .activeYn(entity.isActiveYn())
                .build();
    }
}