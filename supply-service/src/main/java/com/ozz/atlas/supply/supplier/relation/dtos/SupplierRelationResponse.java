package com.ozz.atlas.supply.supplier.relation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Supplier Relation 값 응답")
public class SupplierRelationResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentSupplierPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String parentSupplierName;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String childSupplierPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String childSupplierName;
    @Schema(description = "priority Rank 값", example = "1", nullable = true)
    private Integer priorityRank;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierRelationStatus relationStatus;
    @Schema(description = "effective From 값", example = "sample", nullable = true)
    private LocalDate effectiveFrom;
    @Schema(description = "effective To 값", example = "sample", nullable = true)
    private LocalDate effectiveTo;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
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
