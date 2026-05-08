package com.ozz.atlas.supply.purchaseorder.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Purchase Order 값 검색 조건")
public class PurchaseOrderSearchDto {

    // 요청한 조직 publicId
    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;

    // BUYER / SUPPLIER 뷰 구분
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private PurchaseOrderViewType viewType;

    // 특정 협력사 필터
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;

    // 통합 검색어
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 발주 상태 필터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private PoStatus poStatus;
}
