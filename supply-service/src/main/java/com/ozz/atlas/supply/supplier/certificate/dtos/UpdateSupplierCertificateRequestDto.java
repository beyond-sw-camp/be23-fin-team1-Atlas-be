package com.ozz.atlas.supply.supplier.certificate.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierCertificateRequestDto {
    private String certificateNo;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private String issuerName;
}