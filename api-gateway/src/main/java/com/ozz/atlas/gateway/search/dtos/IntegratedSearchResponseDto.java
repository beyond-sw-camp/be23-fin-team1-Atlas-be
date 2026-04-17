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
public class IntegratedSearchResponseDto {

    // 사용자가 실제로 검색한 키워드
    private String keyword;

    // 사용자, 조직, 업무 데이터를 섹션별로 묶은 결과
    private List<IntegratedSearchSectionDto> sections;
}
