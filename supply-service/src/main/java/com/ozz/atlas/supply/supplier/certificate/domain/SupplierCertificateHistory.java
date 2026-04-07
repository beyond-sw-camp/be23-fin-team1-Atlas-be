package com.ozz.atlas.supply.supplier.certificate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "supplier_certificate_history")
public class SupplierCertificateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_certificate_history_id")
    private Long id;

    @Column(nullable = false)
    private Long supplierCertificateId;

    @Column(nullable = false, length = 30)
    private String actionType; // CREATE, UPDATE, APPROVE, REJECT, EXPIRE

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CertificateStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificateStatus afterStatus;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 26)
    private String actorPublicId;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    public void prePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
    }

    @Builder
    public SupplierCertificateHistory(Long supplierCertificateId, String actionType, CertificateStatus beforeStatus, CertificateStatus afterStatus, String reason, String actorPublicId) {
        this.supplierCertificateId = supplierCertificateId;
        this.actionType = actionType;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.reason = reason;
        this.actorPublicId = actorPublicId;
    }
}