package com.ozz.atlas.supply.search.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedSearchItemDto {

    // 이 결과가 어느 도메인에 속하는지 알려줌
    private IntegratedSearchSectionType type;

    // DB 숫자 ID가 필요한 도메인용 값
    // 예: productionline, settlement
    private Long id;

    // publicId 기반 도메인용 값
    // 예: supplier, item, shipment, return
    private String publicId;

    // 검색 결과 카드에서 가장 크게 보여줄 대표 제목
    private String title;

    // 제목 아래에 보여줄 보조 설명
    private String subtitle;

    // 상태값이 있는 도메인은 문자열로 내려줌
    private String status;
}
