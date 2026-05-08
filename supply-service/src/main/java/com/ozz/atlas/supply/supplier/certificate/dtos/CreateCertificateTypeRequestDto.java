package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유형 요청")
public class CreateCertificateTypeRequestDto {
    @NotBlank(message = "인증 코드는 필수입니다.")
    @Schema(description = "코드", example = "CODE-001")
    private String certificateCode;

    @NotBlank(message = "인증명은 필수입니다.")
    @Schema(description = "이름", example = "샘플 이름")
    private String certificateName;

    @NotNull(message = "인증 범위는 필수입니다.")
    @Schema(description = "유형", example = "DEFAULT")
    private CertificateScope scopeType;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerName;

    @Schema(description = "required Yn 값", example = "true", nullable = true)
    private boolean requiredYn;
    @Schema(description = "active Yn 값", example = "true", nullable = true)
    private boolean activeYn;
}