package com.ozz.atlas.supply.lot.search.document;

import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
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
@Document(indexName = "lots")
@Setting(settingPath = "/elasticsearch/lot-settings.json")
public class LotDocument {

    @Id
    private Long id;

    // LOT publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // LOT 번호
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "lot_ngram_analyzer",
                            searchAnalyzer = "lot_search_analyzer"
                    )
            }
    )
    private String lotNumber;

    // 원본 발주 품목 publicId
    @Field(type = FieldType.Keyword)
    private String sourcePoItemPublicId;

    // 공급사 publicId
    @Field(type = FieldType.Keyword)
    private String supplierPublicId;

    // 품목 publicId
    @Field(type = FieldType.Keyword)
    private String itemPublicId;

    // LOT 상태
    @Field(type = FieldType.Keyword)
    private LotStatus lotStatus;

    // 제조일시
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime manufacturedAt;

    // 만료일시
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime expiredAt;

    // 수량
    @Field(type = FieldType.Double)
    private BigDecimal qty;

    // 단위
    @Field(type = FieldType.Keyword)
    private String unit;

    // 품질 상태
    @Field(type = FieldType.Keyword)
    private QualityStatus qualityStatus;

    // 현재 물류 노드 publicId
    @Field(type = FieldType.Keyword)
    private String currentNodePublicId;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static LotDocument fromEntity(Lot lot) {
        return LotDocument.builder()
                .id(lot.getId())
                .publicId(lot.getPublicId())
                .lotNumber(lot.getLotNumber())
                .sourcePoItemPublicId(lot.getSourcePoItemPublicId())
                .supplierPublicId(lot.getSupplierPublicId())
                .itemPublicId(lot.getItemPublicId())
                .lotStatus(lot.getLotStatus())
                .manufacturedAt(lot.getManufacturedAt())
                .expiredAt(lot.getExpiredAt())
                .qty(lot.getQty())
                .unit(lot.getUnit())
                .qualityStatus(lot.getQualityStatus())
                .currentNodePublicId(lot.getCurrentNodePublicId())
                .createdAt(lot.getCreatedAt())
                .updatedAt(lot.getUpdatedAt())
                .build();
    }
}
