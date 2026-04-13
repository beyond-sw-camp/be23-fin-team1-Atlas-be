package com.ozz.atlas.supply.supplier.certificate.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "certificate_type")
public class CertificateType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_type_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Column(nullable = false, length = 50)
    private String certificateCode;

    @Column(nullable = false, length = 100)
    private String certificateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificateScope scopeType;

    @Column(nullable = false)
    private boolean requiredYn;

    @Column(nullable = false)
    private boolean activeYn;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
    }

    @Builder
    public CertificateType(String certificateCode, String certificateName, CertificateScope scopeType, boolean requiredYn, boolean activeYn) {
        this.certificateCode = certificateCode;
        this.certificateName = certificateName;
        this.scopeType = scopeType;
        this.requiredYn = requiredYn;
        this.activeYn = activeYn;
    }

    public void update(String certificateName, CertificateScope scopeType, Boolean requiredYn, Boolean activeYn) {
        if (certificateName != null) this.certificateName = certificateName;
        if (scopeType != null) this.scopeType = scopeType;
        if (requiredYn != null) this.requiredYn = requiredYn;
        if (activeYn != null) this.activeYn = activeYn;
    }
}