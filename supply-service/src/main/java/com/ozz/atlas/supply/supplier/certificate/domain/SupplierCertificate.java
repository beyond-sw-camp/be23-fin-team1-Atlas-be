package com.ozz.atlas.supply.supplier.certificate.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Long supplierId;

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

    @Column(nullable = false)
    private boolean verifiedYn;

    private LocalDateTime verifiedAt;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.certificateStatus == null) {
            this.certificateStatus = CertificateStatus.VALID;
        }
    }

    @Builder
    public SupplierCertificate(Long supplierId, CertificateType certificateType, String certificateNo, LocalDate issuedAt, LocalDate expiredAt, String issuerName) {
        this.supplierId = supplierId;
        this.certificateType = certificateType;
        this.certificateNo = certificateNo;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.issuerName = issuerName;
        this.verifiedYn = false;
        this.certificateStatus = CertificateStatus.VALID;
    }

    public void verify() {
        this.verifiedYn = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void update(String certificateNo, LocalDate issuedAt, LocalDate expiredAt, String issuerName) {
        if (certificateNo != null) this.certificateNo = certificateNo;
        if (issuedAt != null) this.issuedAt = issuedAt;
        if (expiredAt != null) this.expiredAt = expiredAt;
        if (issuerName != null) this.issuerName = issuerName;
    }
}