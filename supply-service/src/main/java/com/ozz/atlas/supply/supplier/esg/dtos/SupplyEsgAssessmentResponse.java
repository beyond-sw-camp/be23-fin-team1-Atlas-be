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
public class SupplyEsgAssessmentResponse {

    private Long esgAssessmentId;
    private String supplierPublicId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal environmentScore;
    private BigDecimal socialScore;
    private BigDecimal governanceScore;
    private BigDecimal totalScore;
    private EsgGrade grade;
    private LocalDateTime evaluatedAt;
    private String evaluatorName;
    private String note;

    public static SupplyEsgAssessmentResponse fromEntity(SupplyEsgAssessment assessment) {
        return SupplyEsgAssessmentResponse.builder()
                .esgAssessmentId(assessment.getId())
                .supplierPublicId(assessment.getSupplier().getPublicId())
                .supplierCode(assessment.getSupplier().getSupplierCode())
                .supplierName(assessment.getSupplier().getSupplierName())
                .environmentScore(assessment.getEnvironmentScore())
                .socialScore(assessment.getSocialScore())
                .governanceScore(assessment.getGovernanceScore())
                .totalScore(assessment.getTotalScore())
                .grade(assessment.getGrade())
                .evaluatedAt(assessment.getEvaluatedAt())
                .evaluatorName(assessment.getEvaluatorName())
                .note(assessment.getNote())
                .build();
    }
}
