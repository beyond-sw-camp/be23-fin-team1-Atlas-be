package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Supplier Certificate 값 응답")
public class SupplierCertificateResponseDto {
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private CertificateTypeResponseDto certificateType;
    @Schema(description = "certificate No 값", example = "NO-2026-0001", nullable = true)
    private String certificateNo;
    @Schema(description = "issued At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDate issuedAt;
    @Schema(description = "expired At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDate expiredAt;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private CertificateStatus certificateStatus;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerName;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String attachmentPublicId;
    @Schema(description = "사유", example = "샘플 내용", nullable = true)
    private String rejectReason;
    public static SupplierCertificateResponseDto from(SupplierCertificate entity, String supplierName) {
        return SupplierCertificateResponseDto.builder()
                .publicId(entity.getPublicId())
                .supplierPublicId(entity.getSupplierPublicId())
                .supplierName(supplierName)
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