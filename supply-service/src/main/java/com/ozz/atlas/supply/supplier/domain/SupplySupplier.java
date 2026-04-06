package com.ozz.atlas.supply.supplier.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

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
    @Builder.Default
    private SupplierStatus supplierStatus = SupplierStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.REQUESTED;

    @Column(length = 50)
    private String primaryContactName;

    @Column(length = 100)
    private String primaryContactEmail;

    @Column(length = 30)
    private String primaryContactPhone;

    public static SupplySupplier create(
            String organizationPublicId,
            String supplierCode,
            String supplierName,
            Integer tierLevel,
            String primaryContactName,
            String primaryContactEmail,
            String primaryContactPhone
    ) {
        return SupplySupplier.builder()
                .organizationPublicId(organizationPublicId)
                .supplierCode(supplierCode)
                .supplierName(supplierName)
                .tierLevel(tierLevel)
                .supplierStatus(SupplierStatus.INACTIVE)
                .approvalStatus(ApprovalStatus.REQUESTED)
                .primaryContactName(primaryContactName)
                .primaryContactEmail(primaryContactEmail)
                .primaryContactPhone(primaryContactPhone)
                .build();
    }

    public void update(
            String supplierCode,
            String supplierName,
            Integer tierLevel,
            String primaryContactName,
            String primaryContactEmail,
            String primaryContactPhone
    ) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.tierLevel = tierLevel;
        this.primaryContactName = primaryContactName;
        this.primaryContactEmail = primaryContactEmail;
        this.primaryContactPhone = primaryContactPhone;
    }

    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.supplierStatus = SupplierStatus.ACTIVE;
    }
    public void reject() {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.supplierStatus = SupplierStatus.INACTIVE;
    }

    public void softDelete() {
        this.supplierStatus = SupplierStatus.TERMINATED;
    }



}
