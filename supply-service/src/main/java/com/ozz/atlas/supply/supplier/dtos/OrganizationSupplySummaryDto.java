package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Organization Supply Summary 값 모델")
public class OrganizationSupplySummaryDto {

    // 조직에 연결된 창고/물류 거점 수
    @Schema(description = "개수", example = "1", nullable = true)
    private long warehouseCount;

    // 조직에 연결된 인증/ESG 파일 수
    @Schema(description = "개수", example = "1", nullable = true)
    private long esgFileCount;
}
