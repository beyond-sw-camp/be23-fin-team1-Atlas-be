package com.ozz.atlas.supply.returns.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Return 값 검색 조건")
public class ReturnSearchDto {

    // 통합 검색어
    // 반품번호, 반품사유, 상세사유 같은 텍스트 검색에 사용
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 반품 요청 조직 publicId
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String requestOrganizationPublicId;

    // 반품 대상 조직 publicId
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String targetOrganizationPublicId;

    // 원출하 publicId
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String sourceShipmentPublicId;

    // 반품 유형 필터
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private ReturnType returnType;

    // 처리 방식 필터
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private com.ozz.atlas.supply.returns.domain.ResolutionType resolutionType;

    // 반품 상태 필터
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ReturnStatus returnStatus;

    // 반품 품목 publicId 필터
    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;
}
