package com.ozz.atlas.supply.supplier.search.document;

import com.ozz.atlas.supply.supplier.capability.domain.SupplierItemQualityGrade;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.esg.domain.EsgGrade;
import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "suppliers")
@Setting(settingPath = "/elasticsearch/supplier-settings.json")
public class SupplierDocument {

    @Id
    private Long supplierId;

    // 협력사 자체 publicId
    private String publicId;

    // 협력사가 연결된 조직 publicId
    private String organizationPublicId;

    // 협력사 코드
    private String supplierCode;

    // 협력사명
    // main field: 일반 검색용
    // keyword sub field: 정확한 값 비교용
    // ngram sub field: 중간 부분검색용
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "supplier_ngram_analyzer",
                            searchAnalyzer = "supplier_search_analyzer"
                    )
            }
    )
    private String supplierName;

    // 1차, 2차 같은 협력사 단계
    private Integer tierLevel;

    // 승인 요청 / 승인 완료 / 반려 상태
    private ApprovalStatus approvalStatus;

    // 활성 / 비활성 / 중지 / 종료 상태
    private SupplierStatus supplierStatus;

    // 주요 담당자 이름
    private String primaryContactName;

    // 주요 담당자 이메일
    private String primaryContactEmail;

    // 주요 담당자 전화번호
    private String primaryContactPhone;

    // DB LocalDateTime 문자열 형식에 맞춰 ES date로 저장
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // DB LocalDateTime 문자열 형식에 맞춰 ES date로 저장
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    // 유효한 인증서가 하나라도 있는지 빠르게 확인하기 위한 요약 값
    private Boolean hasValidCertificate;

    // 최신 ESG 평가 기준 등급
    private EsgGrade esgGrade;

    // 최신 ESG 총점
    private BigDecimal totalScore;

    // 최신 ESG 평가 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime evaluatedAt;

    // 품목별 공급 역량 목록
    @Field(type = FieldType.Nested)
    private List<CapabilityDocument> capabilities;

    // 인증서별 상태와 유효기간 목록
    @Field(type = FieldType.Nested)
    private List<CertificateDocument> certificates;

    public static SupplierDocument fromEntity(
            SupplySupplier supplier,
            List<SupplySupplierItemCapability> capabilities,
            List<SupplierCertificate> certificates,
            SupplyEsgAssessment latestEsgAssessment
    ) {
        return SupplierDocument.builder()
                .supplierId(supplier.getId())
                .publicId(supplier.getPublicId())
                .organizationPublicId(supplier.getOrganizationPublicId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .tierLevel(supplier.getTierLevel())
                .approvalStatus(supplier.getApprovalStatus())
                .supplierStatus(supplier.getSupplierStatus())
                .primaryContactName(supplier.getPrimaryContactName())
                .primaryContactEmail(supplier.getPrimaryContactEmail())
                .primaryContactPhone(supplier.getPrimaryContactPhone())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .hasValidCertificate(certificates.stream().anyMatch(SupplierDocument::isValidCertificate))
                .esgGrade(latestEsgAssessment != null ? latestEsgAssessment.getGrade() : null)
                .totalScore(latestEsgAssessment != null ? latestEsgAssessment.getTotalScore() : null)
                .evaluatedAt(latestEsgAssessment != null ? latestEsgAssessment.getEvaluatedAt() : null)
                .capabilities(capabilities.stream().map(CapabilityDocument::fromEntity).toList())
                .certificates(certificates.stream().map(CertificateDocument::fromEntity).toList())
                .build();
    }

    private static boolean isValidCertificate(SupplierCertificate certificate) {
        return certificate.getCertificateStatus() == CertificateStatus.APPROVED
                && (certificate.getExpiredAt() == null || !certificate.getExpiredAt().isBefore(LocalDate.now()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CapabilityDocument {
        private String itemPublicId;
        private String itemCode;
        private String itemName;
        private Integer leadTimeDays;
        private BigDecimal monthlyCapacity;
        private BigDecimal availableQty;
        private BigDecimal moq;
        private SupplierItemQualityGrade qualityGrade;
        private BigDecimal unitPriceHint;
        private LocalDate validFrom;

        public static CapabilityDocument fromEntity(SupplySupplierItemCapability capability) {
            return CapabilityDocument.builder()
                    .itemPublicId(capability.getItem().getPublicId())
                    .itemCode(capability.getItem().getItemCode())
                    .itemName(capability.getItem().getItemName())
                    .leadTimeDays(capability.getLeadTimeDays())
                    .monthlyCapacity(capability.getMonthlyCapacity())
                    .availableQty(capability.getAvailableQty())
                    .moq(capability.getMoq())
                    .qualityGrade(capability.getQualityGrade())
                    .unitPriceHint(capability.getUnitPriceHint())
                    .validFrom(capability.getValidFrom())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CertificateDocument {
        private Long certificateTypeId;
        private String certificateTypeCode;
        private String certificateTypeName;
        private CertificateStatus certificateStatus;
        private String certificateNo;
        private LocalDate issuedAt;
        private LocalDate expiredAt;
        private String issuerName;
        private Boolean valid;

        public static CertificateDocument fromEntity(SupplierCertificate certificate) {
            return CertificateDocument.builder()
                    .certificateTypeId(certificate.getCertificateType().getId())
                    .certificateTypeCode(certificate.getCertificateType().getCertificateCode())
                    .certificateTypeName(certificate.getCertificateType().getCertificateName())
                    .certificateStatus(certificate.getCertificateStatus())
                    .certificateNo(certificate.getCertificateNo())
                    .issuedAt(certificate.getIssuedAt())
                    .expiredAt(certificate.getExpiredAt())
                    .issuerName(certificate.getIssuerName())
                    .valid(
                            certificate.getCertificateStatus() == CertificateStatus.APPROVED
                                    && (certificate.getExpiredAt() == null
                                    || !certificate.getExpiredAt().isBefore(LocalDate.now()))
                    )
                    .build();
        }
    }
}
