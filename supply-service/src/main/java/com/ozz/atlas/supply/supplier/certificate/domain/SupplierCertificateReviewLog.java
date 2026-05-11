package com.ozz.atlas.supply.supplier.certificate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "supplier_certificate_review_log",
        indexes = {
                @Index(name = "idx_cert_review_log_certificate", columnList = "supplier_certificate_id,reviewed_at"),
                @Index(name = "idx_cert_review_log_reviewer_org", columnList = "reviewer_organization_public_id")
        }
)
public class SupplierCertificateReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_certificate_review_log_id")
    private Long id;

    @Column(nullable = false)
    private Long supplierCertificateId;

    @Column(nullable = false, length = 26)
    private String supplierCertificatePublicId;

    @Column(length = 100)
    private String certificateNo;

    @Column(length = 26)
    private String certificateTypePublicId;

    @Column(length = 50)
    private String certificateTypeCode;

    @Column(length = 100)
    private String certificateTypeName;

    private LocalDate issuedAt;

    private LocalDate expiredAt;

    @Column(length = 100)
    private String issuerName;

    @Column(length = 26)
    private String attachmentPublicId;

    @Column(nullable = false, length = 26)
    private String supplierPublicId;

    @Column(length = 120)
    private String supplierName;

    @Column(length = 26)
    private String requesterUserPublicId;

    @Column(length = 26)
    private String requesterOrganizationPublicId;

    @Column(length = 120)
    private String requesterOrganizationName;

    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificateStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificateStatus afterStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateReviewDecision decision;

    @Column(length = 26)
    private String reviewerUserPublicId;

    @Column(length = 26)
    private String reviewerOrganizationPublicId;

    @Column(length = 120)
    private String reviewerOrganizationName;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @Builder
    private SupplierCertificateReviewLog(
            Long supplierCertificateId,
            String supplierCertificatePublicId,
            String certificateNo,
            String certificateTypePublicId,
            String certificateTypeCode,
            String certificateTypeName,
            LocalDate issuedAt,
            LocalDate expiredAt,
            String issuerName,
            String attachmentPublicId,
            String supplierPublicId,
            String supplierName,
            String requesterUserPublicId,
            String requesterOrganizationPublicId,
            String requesterOrganizationName,
            LocalDateTime requestedAt,
            CertificateStatus beforeStatus,
            CertificateStatus afterStatus,
            CertificateReviewDecision decision,
            String reviewerUserPublicId,
            String reviewerOrganizationPublicId,
            String reviewerOrganizationName,
            String rejectReason,
            LocalDateTime reviewedAt
    ) {
        this.supplierCertificateId = supplierCertificateId;
        this.supplierCertificatePublicId = supplierCertificatePublicId;
        this.certificateNo = certificateNo;
        this.certificateTypePublicId = certificateTypePublicId;
        this.certificateTypeCode = certificateTypeCode;
        this.certificateTypeName = certificateTypeName;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.issuerName = issuerName;
        this.attachmentPublicId = attachmentPublicId;
        this.supplierPublicId = supplierPublicId;
        this.supplierName = supplierName;
        this.requesterUserPublicId = requesterUserPublicId;
        this.requesterOrganizationPublicId = requesterOrganizationPublicId;
        this.requesterOrganizationName = requesterOrganizationName;
        this.requestedAt = requestedAt;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.decision = decision;
        this.reviewerUserPublicId = reviewerUserPublicId;
        this.reviewerOrganizationPublicId = reviewerOrganizationPublicId;
        this.reviewerOrganizationName = reviewerOrganizationName;
        this.rejectReason = rejectReason;
        this.reviewedAt = reviewedAt;
    }

    public static SupplierCertificateReviewLog of(
            SupplierCertificate certificate,
            String supplierName,
            String requesterOrganizationName,
            CertificateStatus beforeStatus,
            CertificateReviewDecision decision,
            String reviewerUserPublicId,
            String reviewerOrganizationPublicId,
            String reviewerOrganizationName,
            String rejectReason
    ) {
        return SupplierCertificateReviewLog.builder()
                .supplierCertificateId(certificate.getId())
                .supplierCertificatePublicId(certificate.getPublicId())
                .certificateNo(certificate.getCertificateNo())
                .certificateTypePublicId(certificate.getCertificateType().getPublicId())
                .certificateTypeCode(certificate.getCertificateType().getCertificateCode())
                .certificateTypeName(certificate.getCertificateType().getCertificateName())
                .issuedAt(certificate.getIssuedAt())
                .expiredAt(certificate.getExpiredAt())
                .issuerName(certificate.getIssuerName())
                .attachmentPublicId(certificate.getAttachmentPublicId())
                .supplierPublicId(certificate.getSupplierPublicId())
                .supplierName(supplierName)
                .requesterUserPublicId(certificate.getRequestedByUserPublicId())
                .requesterOrganizationPublicId(certificate.getRequestedByOrganizationPublicId())
                .requesterOrganizationName(requesterOrganizationName)
                .requestedAt(certificate.getRequestedAt())
                .beforeStatus(beforeStatus)
                .afterStatus(certificate.getCertificateStatus())
                .decision(decision)
                .reviewerUserPublicId(reviewerUserPublicId)
                .reviewerOrganizationPublicId(reviewerOrganizationPublicId)
                .reviewerOrganizationName(reviewerOrganizationName)
                .rejectReason(rejectReason)
                .reviewedAt(certificate.getReviewedAt())
                .build();
    }
}
