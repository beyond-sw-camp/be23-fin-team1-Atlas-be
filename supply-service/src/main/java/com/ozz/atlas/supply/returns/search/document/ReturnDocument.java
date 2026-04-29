package com.ozz.atlas.supply.returns.search.document;

import com.ozz.atlas.supply.returns.domain.ReturnItem;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
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
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "returns")
@Setting(settingPath = "/elasticsearch/return-settings.json")
public class ReturnDocument {

    @Id
    private Long id;

    // 반품 publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // 반품 번호
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "return_ngram_analyzer",
                            searchAnalyzer = "return_search_analyzer"
                    )
            }
    )
    private String returnNumber;

    // 원출하 publicId
    @Field(type = FieldType.Keyword)
    private String sourceShipmentPublicId;

    @Field(type = FieldType.Keyword)
    private String returnShipmentPublicId;

    // 요청 조직 publicId
    @Field(type = FieldType.Keyword)
    private String requestOrganizationPublicId;

    // 대상 조직 publicId
    @Field(type = FieldType.Keyword)
    private String targetOrganizationPublicId;

    // 반품 유형
    @Field(type = FieldType.Keyword)
    private ReturnType returnType;

    // 반품 사유
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "return_ngram_analyzer",
                            searchAnalyzer = "return_search_analyzer"
                    )
            }
    )
    private String returnReason;

    // 반품 상태
    @Field(type = FieldType.Keyword)
    private ReturnStatus returnStatus;

    // 요청 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime requestedAt;

    // 승인 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime approvedAt;

    // 완료 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime completedAt;

    // 생성 사용자 publicId
    @Field(type = FieldType.Keyword)
    private String createdByUserPublicId;

    // 첨부파일 publicId 목록
    @Field(type = FieldType.Keyword)
    private List<String> attachmentPublicIds;

    // 반품 품목 목록
    @Field(type = FieldType.Nested)
    private List<ReturnItemDocument> items;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static ReturnDocument fromEntity(ReturnRequest returnRequest) {
        return ReturnDocument.builder()
                .id(returnRequest.getId())
                .publicId(returnRequest.getPublicId())
                .returnNumber(returnRequest.getReturnNumber())
                .sourceShipmentPublicId(returnRequest.getSourceShipmentPublicId())
                .returnShipmentPublicId(returnRequest.getReturnShipmentPublicId())
                .requestOrganizationPublicId(returnRequest.getRequestOrganizationPublicId())
                .targetOrganizationPublicId(returnRequest.getTargetOrganizationPublicId())
                .returnType(returnRequest.getReturnType())
                .returnReason(returnRequest.getReturnReason())
                .returnStatus(returnRequest.getReturnStatus())
                .requestedAt(returnRequest.getRequestedAt())
                .approvedAt(returnRequest.getApprovedAt())
                .completedAt(returnRequest.getCompletedAt())
                .createdByUserPublicId(returnRequest.getCreatedByUserPublicId())
                .attachmentPublicIds(splitAttachmentIds(returnRequest.getAttachmentPublicIds()))
                .items(
                        returnRequest.getItems().stream()
                                .map(ReturnItemDocument::fromEntity)
                                .toList()
                )
                .createdAt(returnRequest.getCreatedAt())
                .updatedAt(returnRequest.getUpdatedAt())
                .build();
    }

    private static List<String> splitAttachmentIds(String attachmentPublicIds) {
        if (attachmentPublicIds == null || attachmentPublicIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(attachmentPublicIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReturnItemDocument {

        // 반품 품목 DB id
        @Field(type = FieldType.Long)
        private Long id;

        // 품목 publicId
        @Field(type = FieldType.Keyword)
        private String itemPublicId;

        // 반품 수량
        @Field(type = FieldType.Double)
        private BigDecimal returnQty;

        // 단위
        @Field(type = FieldType.Keyword)
        private String unit;

        // 상세 사유
        @MultiField(
                mainField = @Field(type = FieldType.Text),
                otherFields = {
                        @InnerField(
                                suffix = "ngram",
                                type = FieldType.Text,
                                analyzer = "return_ngram_analyzer",
                                searchAnalyzer = "return_search_analyzer"
                        )
                }
        )
        private String detailReason;

        // 아이템 상태
        @Field(type = FieldType.Keyword)
        private String itemStatus;

        // 첨부파일 publicId 목록
        @Field(type = FieldType.Keyword)
        private List<String> attachmentPublicIds;

        public static ReturnItemDocument fromEntity(ReturnItem item) {
            return ReturnItemDocument.builder()
                    .id(item.getId())
                    .itemPublicId(item.getItemPublicId())
                    .returnQty(item.getReturnQty())
                    .unit(item.getUnit())
                    .detailReason(item.getDetailReason())
                    .itemStatus(item.getItemStatus())
                    .attachmentPublicIds(splitAttachmentIds(item.getAttachmentPublicIds()))
                    .build();
        }
    }
}
