package com.ozz.atlas.supply.productionline.search.dtos;

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
@Schema(description = "Production Line 값 검색 조건")
public class ProductionLineSearchDto {

    // 통합 검색어
    // 현재는 생산라인 코드, 이름, 유형 검색에 사용
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 물류 노드 publicId 필터
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id", nullable = true)
    private String logisticsNodePublicId;

    // 생산라인 유형 필터
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String lineType;

    // 생산라인 상태 필터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
}
