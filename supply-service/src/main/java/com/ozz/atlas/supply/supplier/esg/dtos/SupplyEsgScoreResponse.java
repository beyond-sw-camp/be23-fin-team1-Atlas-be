package com.ozz.atlas.supply.supplier.esg.dtos;

import com.ozz.atlas.supply.supplier.esg.domain.EsgGrade;
import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyEsgScoreResponse {

    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal totalScore;
    private EsgGrade grade;
    private LocalDateTime evaluatedAt;

    public static SupplyEsgScoreResponse fromEntity(SupplyEsgAssessment assessment) {
        return SupplyEsgScoreResponse.builder()
                .supplierPublicId(assessment.getSupplier().getPublicId())
                .supplierCode(assessment.getSupplier().getSupplierCode())
                .supplierName(assessment.getSupplier().getSupplierName())
                .totalScore(assessment.getTotalScore())
                .grade(assessment.getGrade())
                .evaluatedAt(assessment.getEvaluatedAt())
                .build();
    }
}
