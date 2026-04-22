package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateTypeRequestDto {
    @NotBlank(message = "인증 코드는 필수입니다.")
    private String certificateCode;

    @NotBlank(message = "인증명은 필수입니다.")
    private String certificateName;

    @NotNull(message = "인증 범위는 필수입니다.")
    private CertificateScope scopeType;

    private String issuerName;

    private boolean requiredYn;
    private boolean activeYn = true;
}