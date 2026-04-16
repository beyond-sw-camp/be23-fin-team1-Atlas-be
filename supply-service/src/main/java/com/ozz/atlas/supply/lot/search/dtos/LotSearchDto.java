package com.ozz.atlas.supply.lot.search.dtos;

import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LotSearchDto {

    // 통합 검색어
    // lotNumber 검색 중심으로 사용
    private String keyword;

    // 공급사 publicId 필터
    private String supplierPublicId;

    // 품목 publicId 필터
    private String itemPublicId;

    // 원본 발주 품목 publicId 필터
    private String sourcePoItemPublicId;

    // 현재 물류 노드 publicId 필터
    private String currentNodePublicId;

    // LOT 상태 필터
    private LotStatus lotStatus;

    // 품질 상태 필터
    private QualityStatus qualityStatus;
}
