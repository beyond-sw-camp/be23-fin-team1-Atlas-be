package com.ozz.atlas.supply.batch.domain;


import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// 인증서 만료 30일 전 경고 저장
@Getter
@Entity
@Table(
        name = "supply_expiry_warning",
        uniqueConstraints = {
                @UniqueConstraint( // 같은 종류의 경고를 같은 날짜에 중복 저장하지 않기 위한 제약

                        name = "uk_supply_expiry_warning_type_source_date",
                        columnNames = {"warning_type", "source_public_id", "warning_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SupplyExpiryWarning extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Enumerated(EnumType.STRING)
    @Column(name = "warning_type", nullable = false, length = 30)
    private ExpiryWarningType warningType;

    @Column(name = "source_public_id", nullable = false, length = 26)
    private String sourcePublicId; // 원본 객체 publicId (certificate.publicId / lot.publicId)

    @Column(name = "supplier_public_id", nullable = false, length = 26)
    private String supplierPublicId;

    @Column(name = "item_public_id", length = 26)
    private String itemPublicId; // 품목 관련 경고일 때만 사용

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "days_remaining", nullable = false)
    private Integer daysRemaining;

    @Column(name = "warning_date", nullable = false)
    private LocalDate warningDate; // 경고 생성 기준 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExpiryWarningStatus status = ExpiryWarningStatus.OPEN;

    @Builder
    private SupplyExpiryWarning(
            String publicId,
            ExpiryWarningType warningType,
            String sourcePublicId,
            String supplierPublicId,
            String itemPublicId,
            String title,
            String message,
            LocalDate expiryDate,
            Integer daysRemaining,
            LocalDate warningDate,
            ExpiryWarningStatus status
    ) {
        this.publicId = publicId;
        this.warningType = warningType;
        this.sourcePublicId = sourcePublicId;
        this.supplierPublicId = supplierPublicId;
        this.itemPublicId = itemPublicId;
        this.title = title;
        this.message = message;
        this.expiryDate = expiryDate;
        this.daysRemaining = daysRemaining;
        this.warningDate = warningDate;
        this.status = status;
    }

    public static SupplyExpiryWarning fromCertificate(SupplierCertificate certificate, LocalDate warningDate) {
        int daysRemaining = (int) ChronoUnit.DAYS.between(warningDate, certificate.getExpiredAt());

        return SupplyExpiryWarning.builder()
                .warningType(ExpiryWarningType.CERTIFICATE_EXPIRY)
                .sourcePublicId(certificate.getPublicId())
                .supplierPublicId(certificate.getSupplierPublicId())
                .title("인증서 만료 예정")
                .message("인증서가 30일 이내 만료됩니다. certificateNo=" + certificate.getCertificateNo())
                .expiryDate(certificate.getExpiredAt())
                .daysRemaining(daysRemaining)
                .warningDate(warningDate)
                .build();
    }

    public void resolve() {
        this.status = ExpiryWarningStatus.RESOLVED;
    }
}
