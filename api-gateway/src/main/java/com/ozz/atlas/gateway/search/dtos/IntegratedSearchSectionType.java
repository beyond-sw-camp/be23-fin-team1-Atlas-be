package com.ozz.atlas.gateway.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "유형 모델")
public enum IntegratedSearchSectionType {

    // 사용자 검색 결과 섹션
    USER("사용자"),

    // 조직 검색 결과 섹션
    ORGANIZATION("조직"),

    // 공급사 검색 결과 섹션
    SUPPLIER("공급사"),

    // 품목 검색 결과 섹션
    ITEM("품목"),

    // 발주 검색 결과 섹션
    PURCHASE_ORDER("발주"),

    // 출하 검색 결과 섹션
    SHIPMENT("출하"),

    // 반품 검색 결과 섹션
    RETURN("반품"),

    // 생산라인 검색 결과 섹션
    PRODUCTION_LINE("생산라인"),

    // 정산 검색 결과 섹션
    SETTLEMENT("정산");

    // 프론트에 보여줄 섹션 이름
    @Schema(description = "label 값", example = "sample", nullable = true)
    private final String label;
}
