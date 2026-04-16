package com.ozz.atlas.supply.settlement.search.dtos;

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
public class SettlementSearchDto {

    // 통합 검색어
    // 현재는 targetPublicId 검색에 사용
    private String keyword;

    // 공급사 publicId 필터
    private String supplierPublicId;

    // 정산 대상 유형 필터
    private SettlementTargetType targetType;

    // 정산 상태 필터
    private SettlementStatus settlementStatus;

    // 통화 코드 필터
    private SettlementCurrency currencyCode;
}
