package com.ozz.atlas.supply.supplier.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SupplierStatus supplierStatus = SupplierStatus.INACTIVE;

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
            String primaryContactName,
            String primaryContactEmail,
            String primaryContactPhone
    ) {
        return SupplySupplier.builder()
                .organizationPublicId(organizationPublicId)
                .supplierCode(supplierCode)
                .supplierName(supplierName)
                .supplierStatus(SupplierStatus.INACTIVE)
                .primaryContactName(primaryContactName)
                .primaryContactEmail(primaryContactEmail)
                .primaryContactPhone(primaryContactPhone)
                .build();
    }

    public void update(
            String supplierCode,
            String supplierName,
            String primaryContactName,
            String primaryContactEmail,
            String primaryContactPhone
    ) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.primaryContactName = primaryContactName;
        this.primaryContactEmail = primaryContactEmail;
        this.primaryContactPhone = primaryContactPhone;
    }

    public void activate() {
        this.supplierStatus = SupplierStatus.ACTIVE;
    }

    public void deactivate() {
        this.supplierStatus = SupplierStatus.INACTIVE;
    }

    public void softDelete() {
        this.supplierStatus = SupplierStatus.TERMINATED;
    }
}
