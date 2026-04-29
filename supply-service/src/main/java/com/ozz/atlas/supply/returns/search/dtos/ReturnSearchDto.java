package com.ozz.atlas.supply.returns.search.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnSearchDto {

    // 통합 검색어
    // 반품번호, 반품사유, 상세사유 같은 텍스트 검색에 사용
    private String keyword;

    // 반품 요청 조직 publicId
    private String requestOrganizationPublicId;

    // 반품 대상 조직 publicId
    private String targetOrganizationPublicId;

    // 원출하 publicId
    private String sourceShipmentPublicId;

    // 반품 유형 필터
    private ReturnType returnType;

    // 처리 방식 필터
    private com.ozz.atlas.supply.returns.domain.ResolutionType resolutionType;

    // 반품 상태 필터
    private ReturnStatus returnStatus;

    // 반품 품목 publicId 필터
    private String itemPublicId;
}
