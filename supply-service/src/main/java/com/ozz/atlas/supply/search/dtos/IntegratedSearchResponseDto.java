package com.ozz.atlas.supply.search.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedSearchResponseDto {

    // 사용자가 실제로 검색한 키워드
    private String keyword;

    // 섹션별 검색 결과 묶음
    private List<IntegratedSearchSectionDto> sections;
}
