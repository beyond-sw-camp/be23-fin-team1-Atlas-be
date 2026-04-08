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
public class SupplyEsgRankingResponse {

    private Integer rank;
    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal totalScore;
    private EsgGrade grade;
    private LocalDateTime evaluatedAt;

    public static SupplyEsgRankingResponse fromEntity(Integer rank, SupplyEsgAssessment assessment) {
        return SupplyEsgRankingResponse.builder()
                .rank(rank)
                .supplierPublicId(assessment.getSupplier().getPublicId())
                .supplierCode(assessment.getSupplier().getSupplierCode())
                .supplierName(assessment.getSupplier().getSupplierName())
                .totalScore(assessment.getTotalScore())
                .grade(assessment.getGrade())
                .evaluatedAt(assessment.getEvaluatedAt())
                .build();
    }
}
