package com.ozz.atlas.supply.item.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Item 값 검색 조건")
public class ItemSearchDto {

    // 통합 검색어
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 특정 공급사 기준 필터
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;

    // 특정 공급사 조직 기준 필터
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierOrganizationPublicId;

    // 특정 카테고리 기준 필터
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String itemCategoryPublicId;

    // 품목 상태 필터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
}
