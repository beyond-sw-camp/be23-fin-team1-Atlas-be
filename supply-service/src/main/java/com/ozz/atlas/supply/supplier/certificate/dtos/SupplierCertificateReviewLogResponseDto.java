package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateReviewDecision;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateReviewLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "인증서 심사 로그 응답")
public class SupplierCertificateReviewLogResponseDto {

    @Schema(description = "인증서 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierCertificatePublicId;
    @Schema(description = "인증서 번호", example = "CERT-2026-001", nullable = true)
    private String certificateNo;
    @Schema(description = "인증 유형 공개 식별자", example = "sample_public_id", nullable = true)
    private String certificateTypePublicId;
    @Schema(description = "인증 유형 코드", example = "ISO9001", nullable = true)
    private String certificateTypeCode;
    @Schema(description = "인증 유형명", example = "ISO 9001", nullable = true)
    private String certificateTypeName;
    @Schema(description = "발급일", example = "2026-05-11", nullable = true)
    private LocalDate issuedAt;
    @Schema(description = "만료일", example = "2027-05-11", nullable = true)
    private LocalDate expiredAt;
    @Schema(description = "발급 기관", example = "KAB", nullable = true)
    private String issuerName;
    @Schema(description = "첨부 공개 식별자", example = "sample_public_id", nullable = true)
    private String attachmentPublicId;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "협력사명", example = "아틀라스 협력사", nullable = true)
    private String supplierName;
    @Schema(description = "신청자 사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String requesterUserPublicId;
    @Schema(description = "신청 조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String requesterOrganizationPublicId;
    @Schema(description = "신청 조직명", example = "아틀라스 협력사", nullable = true)
    private String requesterOrganizationName;
    @Schema(description = "신청 시각", example = "2026-05-11T16:30:00", nullable = true)
    private LocalDateTime requestedAt;
    @Schema(description = "심사 전 상태", example = "REVIEW_REQUESTED", nullable = true)
    private CertificateStatus beforeStatus;
    @Schema(description = "심사 후 상태", example = "APPROVED", nullable = true)
    private CertificateStatus afterStatus;
    @Schema(description = "심사 판단", example = "APPROVE", nullable = true)
    private CertificateReviewDecision decision;
    @Schema(description = "심사자 사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String reviewerUserPublicId;
    @Schema(description = "심사자 조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String reviewerOrganizationPublicId;
    @Schema(description = "심사자 조직명", example = "아틀라스 본사", nullable = true)
    private String reviewerOrganizationName;
    @Schema(description = "반려 사유", example = "문서 식별 불가", nullable = true)
    private String rejectReason;
    @Schema(description = "심사 시각", example = "2026-05-11T16:30:00", nullable = true)
    private LocalDateTime reviewedAt;

    public static SupplierCertificateReviewLogResponseDto from(SupplierCertificateReviewLog entity) {
        return SupplierCertificateReviewLogResponseDto.builder()
                .supplierCertificatePublicId(entity.getSupplierCertificatePublicId())
                .certificateNo(entity.getCertificateNo())
                .certificateTypePublicId(entity.getCertificateTypePublicId())
                .certificateTypeCode(entity.getCertificateTypeCode())
                .certificateTypeName(entity.getCertificateTypeName())
                .issuedAt(entity.getIssuedAt())
                .expiredAt(entity.getExpiredAt())
                .issuerName(entity.getIssuerName())
                .attachmentPublicId(entity.getAttachmentPublicId())
                .supplierPublicId(entity.getSupplierPublicId())
                .supplierName(entity.getSupplierName())
                .requesterUserPublicId(entity.getRequesterUserPublicId())
                .requesterOrganizationPublicId(entity.getRequesterOrganizationPublicId())
                .requesterOrganizationName(entity.getRequesterOrganizationName())
                .requestedAt(entity.getRequestedAt())
                .beforeStatus(entity.getBeforeStatus())
                .afterStatus(entity.getAfterStatus())
                .decision(entity.getDecision())
                .reviewerUserPublicId(entity.getReviewerUserPublicId())
                .reviewerOrganizationPublicId(entity.getReviewerOrganizationPublicId())
                .reviewerOrganizationName(entity.getReviewerOrganizationName())
                .rejectReason(entity.getRejectReason())
                .reviewedAt(entity.getReviewedAt())
                .build();
    }
}
