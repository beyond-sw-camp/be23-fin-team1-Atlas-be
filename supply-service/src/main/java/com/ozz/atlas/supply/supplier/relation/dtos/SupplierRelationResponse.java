package com.ozz.atlas.supply.supplier.relation.dtos;

import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRelationResponse {

    private String publicId;
    private String parentSupplierPublicId;
    private String parentSupplierName;
    private String childSupplierPublicId;
    private String childSupplierName;
    private Integer priorityRank;
    private SupplierRelationStatus relationStatus;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SupplierRelationResponse fromEntity(SupplySupplierRelation relation) {
        return SupplierRelationResponse.builder()
                .publicId(relation.getPublicId())
                .parentSupplierPublicId(relation.getParentSupplier().getPublicId())
                .parentSupplierName(relation.getParentSupplier().getSupplierName())
                .childSupplierPublicId(relation.getChildSupplier().getPublicId())
                .childSupplierName(relation.getChildSupplier().getSupplierName())
                .priorityRank(relation.getPriorityRank())
                .relationStatus(relation.getRelationStatus())
                .effectiveFrom(relation.getEffectiveFrom())
                .effectiveTo(relation.getEffectiveTo())
                .createdAt(relation.getCreatedAt())
                .updatedAt(relation.getUpdatedAt())
                .build();
    }
}
