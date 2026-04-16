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
public class IntegratedSearchSectionDto {

    // 어떤 도메인 섹션인지 나타냄
    private IntegratedSearchSectionType type;

    // 프론트에 보여줄 섹션 이름
    private String label;

    // 이 섹션에서 전체 검색 결과가 몇 개인지 나타냄
    private long totalCount;

    // 이 섹션에 실제로 내려줄 검색 결과 목록
    private List<IntegratedSearchItemDto> items;
}
