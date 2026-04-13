package com.ozz.atlas.supply.supplier.certificate.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierCertificateRequestDto {
    @NotBlank(message = "인증 유형 ID는 필수입니다.")
    private String certificateTypePublicId;

    private String certificateNo;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private String issuerName;
    private String attachmentPublicId;
}