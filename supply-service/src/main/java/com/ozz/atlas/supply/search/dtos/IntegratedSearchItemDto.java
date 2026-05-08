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
@Schema(description = "Integrated Search Item 값 모델")
public class IntegratedSearchItemDto {

    // 이 결과가 어느 도메인에 속하는지 알려줌
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private IntegratedSearchSectionType type;

    // DB 숫자 ID가 필요한 도메인용 값
    // 예: productionline, settlement
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;

    // publicId 기반 도메인용 값
    // 예: supplier, item, shipment, return
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;

    // 검색 결과 카드에서 가장 크게 보여줄 대표 제목
    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String title;

    // 제목 아래에 보여줄 보조 설명
    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String subtitle;

    // 상태값이 있는 도메인은 문자열로 내려줌
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String status;
}
