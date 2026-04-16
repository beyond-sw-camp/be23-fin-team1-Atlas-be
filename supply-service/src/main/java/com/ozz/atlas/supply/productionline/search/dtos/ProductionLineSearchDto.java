package com.ozz.atlas.supply.productionline.search.dtos;

import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductionLineSearchDto {

    // 통합 검색어
    // 현재는 생산라인 코드, 이름, 유형 검색에 사용
    private String keyword;

    // 물류 노드 publicId 필터
    private String logisticsNodePublicId;

    // 생산라인 유형 필터
    private String lineType;

    // 생산라인 상태 필터
    private Status status;
}
