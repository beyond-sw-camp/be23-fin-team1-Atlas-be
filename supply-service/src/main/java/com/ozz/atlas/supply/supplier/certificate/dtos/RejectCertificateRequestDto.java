package com.ozz.atlas.supply.supplier.certificate.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RejectCertificateRequestDto {
    @NotBlank(message = "반려 사유는 필수입니다.")
    private String rejectReason;
}