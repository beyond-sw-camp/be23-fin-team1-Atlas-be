package com.ozz.atlas.supply.supplier.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSupplySummaryDto {

    // 조직에 연결된 창고/물류 거점 수
    private long warehouseCount;

    // 조직에 연결된 인증/ESG 파일 수
    private long esgFileCount;
}
