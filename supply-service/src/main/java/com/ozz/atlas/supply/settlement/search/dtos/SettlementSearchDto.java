package com.ozz.atlas.supply.settlement.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Settlement 값 검색 조건")
public class SettlementSearchDto {

    // 통합 검색어
    // 현재는 targetPublicId 검색에 사용
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 공급사 publicId 필터
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;

    // 정산 대상 유형 필터
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private SettlementTargetType targetType;

    // 정산 상태 필터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SettlementStatus settlementStatus;

    // 통화 코드 필터
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private SettlementCurrency currencyCode;
}
