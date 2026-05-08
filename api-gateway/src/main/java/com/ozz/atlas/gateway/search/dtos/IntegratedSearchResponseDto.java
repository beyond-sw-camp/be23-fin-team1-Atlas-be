package com.ozz.atlas.gateway.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Integrated Search 값 응답")
public class IntegratedSearchResponseDto {

    // 사용자가 실제로 검색한 키워드
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 사용자, 조직, 업무 데이터를 섹션별로 묶은 결과
    @Schema(description = "검색 섹션 목록", nullable = true)
    private List<IntegratedSearchSectionDto> sections;
}
