package com.ozz.atlas.supply.search.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedSearchRequestDto {

    // 사용자가 입력한 통합 검색어
    private String keyword;

    // 각 섹션마다 몇 개까지 보여줄지 정하는 값
    @Builder.Default
    private Integer size = 5;
}
