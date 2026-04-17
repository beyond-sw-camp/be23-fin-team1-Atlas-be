package com.ozz.atlas.gateway.search.dtos;

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

    // 어떤 섹션인지 구분하는 타입
    private IntegratedSearchSectionType type;

    // 프론트에 보여줄 섹션 이름
    private String label;

    // 이 섹션에서 실제로 내려주는 결과 개수
    private long totalCount;

    // 이 섹션에 포함된 검색 결과 목록
    private List<IntegratedSearchItemDto> items;
}
