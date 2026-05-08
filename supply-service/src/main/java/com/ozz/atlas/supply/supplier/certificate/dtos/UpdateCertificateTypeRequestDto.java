package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유형 요청")
public class UpdateCertificateTypeRequestDto {
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String certificateName;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private CertificateScope scopeType;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerName;
    @Schema(description = "required Yn 값", example = "true", nullable = true)
    private Boolean requiredYn;
    @Schema(description = "active Yn 값", example = "true", nullable = true)
    private Boolean activeYn;
}