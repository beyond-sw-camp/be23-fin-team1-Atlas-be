package com.ozz.atlas.supply.purchaseorder.search.dtos;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PriorityCode;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseOrderSearchDto {

    // 요청한 조직 publicId
    private String organizationPublicId;

    // BUYER / SUPPLIER 뷰 구분
    private PurchaseOrderViewType viewType;

    // 특정 협력사 필터
    private String supplierPublicId;

    // 통합 검색어
    private String keyword;

    // 발주 상태 필터
    private PoStatus poStatus;

    // 우선순위 필터
    private PriorityCode priorityCode;
}
