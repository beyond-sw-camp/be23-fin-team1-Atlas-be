package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SupplierCertificateResponseDto {
    private String publicId;
    private String supplierPublicId;
    private CertificateTypeResponseDto certificateType;
    private String certificateNo;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private CertificateStatus certificateStatus;
    private String issuerName;
    private String attachmentPublicId;
    private String rejectReason;

    public static SupplierCertificateResponseDto from(SupplierCertificate entity) {
        return SupplierCertificateResponseDto.builder()
                .publicId(entity.getPublicId())
                .supplierPublicId(entity.getSupplierPublicId())
                .certificateType(CertificateTypeResponseDto.from(entity.getCertificateType()))
                .certificateNo(entity.getCertificateNo())
                .issuedAt(entity.getIssuedAt())
                .expiredAt(entity.getExpiredAt())
                .certificateStatus(entity.getCertificateStatus())
                .issuerName(entity.getIssuerName())
                .attachmentPublicId(entity.getAttachmentPublicId())
                .rejectReason(entity.getRejectReason())
                .build();
    }
}