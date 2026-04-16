package com.ozz.atlas.supply.settlement.search.document;

import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementDetail;
import com.ozz.atlas.supply.settlement.domain.SettlementDetailStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
@Document(indexName = "settlements")
@Setting(settingPath = "/elasticsearch/settlement-settings.json")
public class SettlementDocument {

    @Id
    private Long id;

    // 공급사 publicId
    @Field(type = FieldType.Keyword)
    private String supplierPublicId;

    // 정산 대상 유형
    @Field(type = FieldType.Keyword)
    private SettlementTargetType targetType;

    // 정산 대상 publicId
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "settlement_ngram_analyzer",
                            searchAnalyzer = "settlement_search_analyzer"
                    )
            }
    )
    private String targetPublicId;

    // 정산 기간 시작일
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd")
    private LocalDate settlementPeriodStart;

    // 정산 기간 종료일
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd")
    private LocalDate settlementPeriodEnd;

    // 정산 금액
    @Field(type = FieldType.Double)
    private BigDecimal amount;

    // 통화 코드
    @Field(type = FieldType.Keyword)
    private SettlementCurrency currencyCode;

    // 정산 상태
    @Field(type = FieldType.Keyword)
    private SettlementStatus settlementStatus;

    // 정산 승인 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime settledAt;

    // 승인 사용자 publicId
    @Field(type = FieldType.Keyword)
    private String approvedByUserPublicId;

    // 정산 취소 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime cancelledAt;

    // 취소 사용자 publicId
    @Field(type = FieldType.Keyword)
    private String cancelledByUserPublicId;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    // 정산 상세 목록
    @Field(type = FieldType.Nested)
    private List<SettlementDetailDocument> details;

    public static SettlementDocument fromEntity(
            Settlement settlement,
            String supplierPublicId,
            List<SettlementDetail> details
    ) {
        return SettlementDocument.builder()
                .id(settlement.getId())
                .supplierPublicId(supplierPublicId)
                .targetType(settlement.getTargetType())
                .targetPublicId(settlement.getTargetPublicId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .amount(settlement.getAmount())
                .currencyCode(settlement.getCurrencyCode())
                .settlementStatus(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .approvedByUserPublicId(settlement.getApprovedByUserPublicId())
                .cancelledAt(settlement.getCancelledAt())
                .cancelledByUserPublicId(settlement.getCancelledByUserPublicId())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .details(
                        details == null
                                ? List.of()
                                : details.stream()
                                .map(SettlementDetailDocument::fromEntity)
                                .toList()
                )
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SettlementDetailDocument {

        // 정산 상세 publicId
        @Field(type = FieldType.Keyword)
        private String publicId;

        // 발주 품목 id
        @Field(type = FieldType.Long)
        private Long poItemId;

        // 품목 id
        @Field(type = FieldType.Long)
        private Long itemId;

        // 수량
        @Field(type = FieldType.Double)
        private BigDecimal qty;

        // 단가
        @Field(type = FieldType.Double)
        private BigDecimal unitPrice;

        // 금액
        @Field(type = FieldType.Double)
        private BigDecimal amount;

        // 상세 상태
        @Field(type = FieldType.Keyword)
        private SettlementDetailStatus detailStatus;

        public static SettlementDetailDocument fromEntity(SettlementDetail detail) {
            return SettlementDetailDocument.builder()
                    .publicId(detail.getPublicId())
                    .poItemId(detail.getPoItemId())
                    .itemId(detail.getItemId())
                    .qty(detail.getQty())
                    .unitPrice(detail.getUnitPrice())
                    .amount(detail.getAmount())
                    .detailStatus(detail.getDetailStatus())
                    .build();
        }
    }
}
