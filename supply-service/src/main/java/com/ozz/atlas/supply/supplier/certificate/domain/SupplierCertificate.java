package com.ozz.atlas.supply.supplier.certificate.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "supplier_certificate")
public class SupplierCertificate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_certificate_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Column(nullable = false, length = 26)
    private String supplierPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_type_id", nullable = false)
    private CertificateType certificateType;

    @Column(length = 100)
    private String certificateNo;

    private LocalDate issuedAt;

    private LocalDate expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateStatus certificateStatus;

    @Column(length = 100)
    private String issuerName;

    @Column(length = 26)
    private String attachmentPublicId;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.certificateStatus == null) {
            this.certificateStatus = CertificateStatus.REVIEW_REQUESTED;
        }
    }

    @Builder
    public SupplierCertificate(String supplierPublicId, CertificateType certificateType, String certificateNo, LocalDate issuedAt, LocalDate expiredAt, String issuerName, String attachmentPublicId) {
        this.supplierPublicId = supplierPublicId;
        this.certificateType = certificateType;
        this.certificateNo = certificateNo;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.issuerName = issuerName;
        this.attachmentPublicId = attachmentPublicId;
        this.certificateStatus = CertificateStatus.REVIEW_REQUESTED;
    }

    public void approve() {
        this.certificateStatus = CertificateStatus.APPROVED;
        this.rejectReason = null; // Clear rejection reason if any
    }

    public void reject(String rejectReason) {
        this.certificateStatus = CertificateStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    public void update(String certificateNo, LocalDate issuedAt, LocalDate expiredAt, String issuerName, String attachmentPublicId) {
        if (certificateNo != null) this.certificateNo = certificateNo;
        if (issuedAt != null) this.issuedAt = issuedAt;
        if (expiredAt != null) this.expiredAt = expiredAt;
        if (issuerName != null) this.issuerName = issuerName;
        if (attachmentPublicId != null) this.attachmentPublicId = attachmentPublicId;
        
        // Re-request review when updated
        this.certificateStatus = CertificateStatus.REVIEW_REQUESTED;
    }
}