package com.ozz.atlas.supply.item.search.dtos;

import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSearchDto {

    // 통합 검색어
    private String keyword;

    // 특정 공급사 기준 필터
    private String supplierPublicId;

    // 특정 공급사 조직 기준 필터
    private String supplierOrganizationPublicId;

    // 특정 카테고리 기준 필터
    private String itemCategoryPublicId;

    // 품목 상태 필터
    private Status status;
}
