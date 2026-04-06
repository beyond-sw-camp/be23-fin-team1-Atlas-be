package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String publicId;
    private String organizationPublicId;
    private String supplierCode;
    private String supplierName;
    private Integer tierLevel;
    private SupplierStatus supplierStatus;
    private ApprovalStatus approvalStatus;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SupplierResponse fromEntity(SupplySupplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .publicId(supplier.getPublicId())
                .organizationPublicId(supplier.getOrganizationPublicId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .tierLevel(supplier.getTierLevel())
                .supplierStatus(supplier.getSupplierStatus())
                .approvalStatus(supplier.getApprovalStatus())
                .primaryContactName(supplier.getPrimaryContactName())
                .primaryContactEmail(supplier.getPrimaryContactEmail())
                .primaryContactPhone(supplier.getPrimaryContactPhone())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
