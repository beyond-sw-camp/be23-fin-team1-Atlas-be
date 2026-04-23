package com.ozz.atlas.supply.purchaseorder.search.document;

import com.ozz.atlas.supply.purchaseorder.domain.CurrencyCode;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
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
@Document(indexName = "purchase-orders")
@Setting(settingPath = "/elasticsearch/purchase-order-settings.json")
public class PurchaseOrderDocument {

    @Id
    private Long id;

    // 발주 publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // 발주번호는 가장 핵심 검색 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "purchase_order_ngram_analyzer",
                            searchAnalyzer = "purchase_order_search_analyzer"
                    )
            }
    )
    private String poNumber;

    // 발주사 조직 publicId
    @Field(type = FieldType.Keyword)
    private String buyerOrganizationPublicId;

    // 협력사 조직 publicId
    @Field(type = FieldType.Keyword)
    private String supplierOrganizationPublicId;

    // 협력사 publicId
    @Field(type = FieldType.Keyword)
    private String supplierPublicId;

    // 협력사 코드는 검색에 자주 쓰일 수 있음
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "purchase_order_ngram_analyzer",
                            searchAnalyzer = "purchase_order_search_analyzer"
                    )
            }
    )
    private String supplierCode;

    // 협력사 이름도 부분 검색 가능하게 둠
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "purchase_order_ngram_analyzer",
                            searchAnalyzer = "purchase_order_search_analyzer"
                    )
            }
    )
    private String supplierName;

    // 발주 상태 필터용
    @Field(type = FieldType.Keyword)
    private PoStatus poStatus;

    // 발주 일시
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime orderedAt;

    // 총 금액
    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    // 통화 코드
    @Field(type = FieldType.Keyword)
    private CurrencyCode currencyCode;

    // 메모도 키워드 검색 대상
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "purchase_order_ngram_analyzer",
                            searchAnalyzer = "purchase_order_search_analyzer"
                    )
            }
    )
    private String memo;

    // 생성자 user publicId
    @Field(type = FieldType.Keyword)
    private String createdByUserPublicId;

    // 아이템 publicId는 간단 필터용으로 따로 둠
    @Field(type = FieldType.Keyword)
    private List<String> itemPublicIds;

    // 아이템 코드/이름 검색은 nested 구조로 둠
    @Field(type = FieldType.Nested)
    private List<PurchaseOrderItemDocument> items;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static PurchaseOrderDocument fromEntity(SupplyPurchaseOrder purchaseOrder) {
        return PurchaseOrderDocument.builder()
                .id(purchaseOrder.getId())
                .publicId(purchaseOrder.getPublicId())
                .poNumber(purchaseOrder.getPoNumber())
                .buyerOrganizationPublicId(purchaseOrder.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(purchaseOrder.getSupplier().getOrganizationPublicId())
                .supplierPublicId(purchaseOrder.getSupplier().getPublicId())
                .supplierCode(purchaseOrder.getSupplier().getSupplierCode())
                .supplierName(purchaseOrder.getSupplier().getSupplierName())
                .poStatus(purchaseOrder.getPoStatus())
                .orderedAt(purchaseOrder.getOrderedAt())
                .totalAmount(purchaseOrder.getTotalAmount())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .memo(purchaseOrder.getMemo())
                .createdByUserPublicId(purchaseOrder.getCreatedByUserPublicId())
                .itemPublicIds(
                        purchaseOrder.getActiveItems().stream()
                                .map(item -> item.getItem().getPublicId())
                                .toList()
                )
                .items(
                        purchaseOrder.getActiveItems().stream()
                                .map(PurchaseOrderItemDocument::fromEntity)
                                .toList()
                )
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PurchaseOrderItemDocument {

        // 품목 publicId
        @Field(type = FieldType.Keyword)
        private String itemPublicId;

        // 품목 코드는 검색어로 많이 쓸 수 있음
        @MultiField(
                mainField = @Field(type = FieldType.Text),
                otherFields = {
                        @InnerField(suffix = "keyword", type = FieldType.Keyword),
                        @InnerField(
                                suffix = "ngram",
                                type = FieldType.Text,
                                analyzer = "purchase_order_ngram_analyzer",
                                searchAnalyzer = "purchase_order_search_analyzer"
                        )
                }
        )
        private String itemCode;

        // 품목명도 부분 검색 가능하게 둠
        @MultiField(
                mainField = @Field(type = FieldType.Text),
                otherFields = {
                        @InnerField(suffix = "keyword", type = FieldType.Keyword),
                        @InnerField(
                                suffix = "ngram",
                                type = FieldType.Text,
                                analyzer = "purchase_order_ngram_analyzer",
                                searchAnalyzer = "purchase_order_search_analyzer"
                        )
                }
        )
        private String itemName;

        public static PurchaseOrderItemDocument fromEntity(SupplyPurchaseOrderItem purchaseOrderItem) {
            return PurchaseOrderItemDocument.builder()
                    .itemPublicId(purchaseOrderItem.getItem().getPublicId())
                    .itemCode(purchaseOrderItem.getItem().getItemCode())
                    .itemName(purchaseOrderItem.getItem().getItemName())
                    .build();
        }
    }
}
