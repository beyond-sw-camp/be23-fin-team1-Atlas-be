package com.ozz.atlas.supply.supplier.certificate.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierCertificateRequestDto {
    @NotNull(message = "인증 유형 ID는 필수입니다.")
    private Long certificateTypeId;

    private String certificateNo;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private String issuerName;
    private String attachmentPublicId;
}