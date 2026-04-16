package com.ozz.atlas.supply.productionline.search.document;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
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
@Document(indexName = "production-lines")
@Setting(settingPath = "/elasticsearch/production-line-settings.json")
public class ProductionLineDocument {

    @Id
    private Long id;

    // 물류 노드 publicId
    @Field(type = FieldType.Keyword)
    private String logisticsNodePublicId;

    // 생산라인 코드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "production_line_ngram_analyzer",
                            searchAnalyzer = "production_line_search_analyzer"
                    )
            }
    )
    private String lineCode;

    // 생산라인 이름
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "production_line_ngram_analyzer",
                            searchAnalyzer = "production_line_search_analyzer"
                    )
            }
    )
    private String lineName;

    // 생산라인 유형
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "production_line_ngram_analyzer",
                            searchAnalyzer = "production_line_search_analyzer"
                    )
            }
    )
    private String lineType;

    // 생산라인 상태
    @Field(type = FieldType.Keyword)
    private Status status;

    // 일일 생산 가능량
    @Field(type = FieldType.Double)
    private BigDecimal dailyCapacity;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static ProductionLineDocument fromEntity(ProductionLine productionLine) {
        return ProductionLineDocument.builder()
                .id(productionLine.getProductionLineId())
                .logisticsNodePublicId(productionLine.getLogisticsNodePublicId())
                .lineCode(productionLine.getLineCode())
                .lineName(productionLine.getLineName())
                .lineType(productionLine.getLineType())
                .status(productionLine.getStatus())
                .dailyCapacity(productionLine.getDailyCapacity())
                .createdAt(productionLine.getCreatedAt())
                .updatedAt(productionLine.getUpdatedAt())
                .build();
    }
}
