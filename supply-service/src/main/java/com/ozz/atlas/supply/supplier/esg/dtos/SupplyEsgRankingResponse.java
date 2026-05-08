package com.ozz.atlas.supply.supplier.esg.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Supply Esg Ranking 값 응답")
public class SupplyEsgRankingResponse {

    @Schema(description = "rank 값", example = "1", nullable = true)
    private Integer rank;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "합계", example = "1", nullable = true)
    private BigDecimal totalScore;
    @Schema(description = "grade 값", example = "sample", nullable = true)
    private EsgGrade grade;
    @Schema(description = "evaluated At 값", example = "2026-05-08T10:00:00", nullable = true)
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
