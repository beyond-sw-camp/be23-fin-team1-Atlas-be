package com.ozz.atlas.supply.item.search.document;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.ItemUnit;
import com.ozz.atlas.supply.item.domain.SupplyItem;
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
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "items")
@Setting(settingPath = "/elasticsearch/item-settings.json")
public class ItemDocument {

    @Id
    private Long id;

    // 품목 publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // 공급사 publicId
    @Field(type = FieldType.Keyword)
    private String supplierPublicId;

    // 공급사 조직 publicId
    @Field(type = FieldType.Keyword)
    private String supplierOrganizationPublicId;

    // 공급사명
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "item_ngram_analyzer",
                            searchAnalyzer = "item_search_analyzer"
                    )
            }
    )
    private String supplierName;

    // 카테고리 publicId
    @Field(type = FieldType.Keyword)
    private String itemCategoryPublicId;

    // 카테고리명
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "item_ngram_analyzer",
                            searchAnalyzer = "item_search_analyzer"
                    )
            }
    )
    private String categoryName;

    // 품목 코드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "item_ngram_analyzer",
                            searchAnalyzer = "item_search_analyzer"
                    )
            }
    )
    private String itemCode;

    // 품목명
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "item_ngram_analyzer",
                            searchAnalyzer = "item_search_analyzer"
                    )
            }
    )
    private String itemName;

    // 단위
    @Field(type = FieldType.Keyword)
    private ItemUnit unit;

    // 규격
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "item_ngram_analyzer",
                            searchAnalyzer = "item_search_analyzer"
                    )
            }
    )
    private String spec;

    // 유통기한 일수
    @Field(type = FieldType.Integer)
    private Integer shelfLifeDays;

    // 품목 상태
    @Field(type = FieldType.Keyword)
    private Status status;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Double)
    private BigDecimal unitPrice;

    public static ItemDocument fromEntity(SupplyItem item) {
        return ItemDocument.builder()
                .id(item.getId())
                .publicId(item.getPublicId())
                .supplierPublicId(item.getSupplier().getPublicId())
                .supplierOrganizationPublicId(item.getSupplier().getOrganizationPublicId())
                .supplierName(item.getSupplier().getSupplierName())
                .itemCategoryPublicId(item.getItemCategory().getPublicId())
                .categoryName(item.getItemCategory().getCategoryName())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .unit(item.getUnit())
                .spec(item.getSpec())
                .shelfLifeDays(item.getShelfLifeDays())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}
