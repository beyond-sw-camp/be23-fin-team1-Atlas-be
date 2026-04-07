package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SupplierCertificateResponseDto {
    private Long id;
    private String publicId;
    private Long supplierId;
    private CertificateTypeResponseDto certificateType;
    private String certificateNo;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private CertificateStatus certificateStatus;
    private String issuerName;
    private boolean verifiedYn;
    private LocalDateTime verifiedAt;

    public static SupplierCertificateResponseDto from(SupplierCertificate entity) {
        return SupplierCertificateResponseDto.builder()
                .id(entity.getId())
                .publicId(entity.getPublicId())
                .supplierId(entity.getSupplierId())
                .certificateType(CertificateTypeResponseDto.from(entity.getCertificateType()))
                .certificateNo(entity.getCertificateNo())
                .issuedAt(entity.getIssuedAt())
                .expiredAt(entity.getExpiredAt())
                .certificateStatus(entity.getCertificateStatus())
                .issuerName(entity.getIssuerName())
                .verifiedYn(entity.isVerifiedYn())
                .verifiedAt(entity.getVerifiedAt())
                .build();
    }
}