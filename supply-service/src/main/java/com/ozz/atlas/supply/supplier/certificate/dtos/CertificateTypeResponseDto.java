package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유형 응답")
public class CertificateTypeResponseDto {
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String certificateCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String certificateName;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private CertificateScope scopeType;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerName;
    @Schema(description = "required Yn 값", example = "true", nullable = true)
    private boolean requiredYn;
    @Schema(description = "active Yn 값", example = "true", nullable = true)
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