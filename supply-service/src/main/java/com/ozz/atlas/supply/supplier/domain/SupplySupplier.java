package com.ozz.atlas.supply.supplier.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class SupplySupplier extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Column(nullable = false, length = 26)
    private String organizationPublicId;

    @Column(nullable = false, length = 50)
    private String supplierCode;

    @Column(nullable = false, length = 100)
    private String supplierName;

    @Column(nullable = false)
    private Integer tierLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus supplierStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(length = 50)
    private String primaryContactName;

    @Column(length = 100)
    private String primaryContactEmail;

    @Column(length = 30)
    private String primaryContactPhone;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null || this.publicId.isBlank()) {
            this.publicId = PublicIdGenerator.next();
        }
        if (this.supplierStatus == null) {
            this.supplierStatus = SupplierStatus.ACTIVE;
        }
        if (this.approvalStatus == null) {
            this.approvalStatus = ApprovalStatus.APPROVED;
        }
    }
}
