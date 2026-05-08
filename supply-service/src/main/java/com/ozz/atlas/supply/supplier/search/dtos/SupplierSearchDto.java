package com.ozz.atlas.supply.supplier.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.esg.domain.EsgGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Supplier 값 검색 조건")
public class SupplierSearchDto {

    // 협력사명, 코드, 담당자명, 이메일, 전화번호 같은 기본 키워드
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    // 활성, 비활성, 중지, 종료 같은 협력사 상태
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierStatus supplierStatus;

    // 특정 조직에 소속된 협력사만 조회할 때 사용하는 조직 publicId
    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;

    // 특정 품목을 공급할 수 있는 협력사만 찾을 때 사용하는 품목 publicId
    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;

    // 월 생산 가능 수량이 이 값 이상인 협력사
    @Schema(description = "min Monthly Capacity 값", example = "1", nullable = true)
    private Long minMonthlyCapacity;

    // 현재 공급 가능한 수량이 이 값 이상인 협력사
    @Schema(description = "수량", example = "1", nullable = true)
    private Long minAvailableQty;

    // 최소 주문 수량(MOQ)이 이 값 이하인 협력사
    @Schema(description = "max Moq 값", example = "1", nullable = true)
    private Long maxMoq;

    // 리드타임이 이 값 이하인 협력사
    @Schema(description = "max Lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer maxLeadTimeDays;

    // 품질 등급이 특정 기준인 협력사
    @Schema(description = "quality Grade 값", example = "sample", nullable = true)
    private SupplierItemQualityGrade qualityGrade;

    // 특정 인증서 종류를 가진 협력사만 찾을 때 사용하는 인증서 타입 ID
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long certificateTypeId;

    // 인증서 승인 상태
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private CertificateStatus certificateStatus;

    // 현재 유효한 인증서만 가진 협력사만 조회할지 여부
    @Schema(description = "식별자", example = "true", nullable = true)
    private Boolean onlyValidCertificate;

    // ESG 등급 기준
    @Schema(description = "esg Grade 값", example = "sample", nullable = true)
    private EsgGrade esgGrade;

    // ESG 총점이 이 값 이상인 협력사
    @Schema(description = "합계", example = "1", nullable = true)
    private BigDecimal minTotalScore;
}
