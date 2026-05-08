package com.ozz.atlas.supply.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Integrated Search 값 요청")
public class IntegratedSearchRequestDto {

    // 사용자가 입력한 통합 검색어
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 각 섹션마다 몇 개까지 보여줄지 정하는 값
    @Builder.Default
    @Schema(description = "size 값", example = "1", nullable = true)
    private Integer size = 5;
}
