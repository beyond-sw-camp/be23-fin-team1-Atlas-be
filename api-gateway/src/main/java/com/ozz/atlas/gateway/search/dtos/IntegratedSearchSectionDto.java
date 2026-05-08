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
@Schema(description = "Integrated Search Section 값 모델")
public class IntegratedSearchSectionDto {

    // 어떤 섹션인지 구분하는 타입
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private IntegratedSearchSectionType type;

    // 프론트에 보여줄 섹션 이름
    @Schema(description = "label 값", example = "sample", nullable = true)
    private String label;

    // 이 섹션에서 실제로 내려주는 결과 개수
    @Schema(description = "개수", example = "1", nullable = true)
    private long totalCount;

    // 이 섹션에 포함된 검색 결과 목록
    @Schema(description = "항목 목록", nullable = true)
    private List<IntegratedSearchItemDto> items;
}
