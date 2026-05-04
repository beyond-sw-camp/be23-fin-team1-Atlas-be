package com.ozz.atlas.supply.shipment.search.document;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "shipments")
@Setting(settingPath = "/elasticsearch/shipment-settings.json")
public class ShipmentDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String publicId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String shipmentNumber;

    @Field(type = FieldType.Long)
    private Long poId;

    @Field(type = FieldType.Keyword)
    private String purchaseOrderPublicId;

    @Field(type = FieldType.Long)
    private Long subPoId;

    @Field(type = FieldType.Keyword)
    private String subPurchaseOrderPublicId;

    @Field(type = FieldType.Keyword)
    private ShipmentSourceType sourceType;

    @Field(type = FieldType.Keyword)
    private String sourcePublicId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String carrierName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String vehicleNo;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String trackingNo;

    @Field(type = FieldType.Keyword)
    private String originOrganizationPublicId;

    @Field(type = FieldType.Keyword)
    private String originNodePublicId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String originNodeName;
    @Field(type = FieldType.Keyword)
    private String originNodeCode;

    @Field(type = FieldType.Keyword)
    private String destinationOrganizationPublicId;

    @Field(type = FieldType.Keyword)
    private String destinationNodePublicId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String destinationNodeName;
    @Field(type = FieldType.Keyword)
    private String destinationNodeCode;

    @Field(type = FieldType.Keyword)
    private String currentNodePublicId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "shipment_ngram_analyzer",
                            searchAnalyzer = "shipment_search_analyzer"
                    )
            }
    )
    private String currentNodeName;
    @Field(type = FieldType.Keyword)
    private String currentNodeCode;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime departureEta;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime arrivalEta;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime actualDepartedAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime actualArrivedAt;

    @Field(type = FieldType.Keyword)
    private ShipmentStatus status;

    @Field(type = FieldType.Boolean)
    private Boolean temperatureRequired;

    @Field(type = FieldType.Boolean)
    private Boolean sealedPackagingRequired;

    @Field(type = FieldType.Boolean)
    private Boolean fragile;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static ShipmentDocument fromEntity(
            Shipment shipment,
            LogisticsNode originNode,
            LogisticsNode destinationNode,
            LogisticsNode currentNode
    ) {
        return ShipmentDocument.builder()
                .id(shipment.getId())
                .publicId(shipment.getPublicId())
                .shipmentNumber(shipment.getShipmentNumber())
                .poId(shipment.getPoId())
                .purchaseOrderPublicId(shipment.getPurchaseOrderPublicId())
                .subPoId(shipment.getSubPoId())
                .subPurchaseOrderPublicId(shipment.getSubPurchaseOrderPublicId())
                .sourceType(shipment.getSourceType() != null ? shipment.getSourceType() : ShipmentSourceType.ORDER)
                .sourcePublicId(shipment.getSourcePublicId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originOrganizationPublicId(originNode != null ? originNode.getOrganizationPublicId() : null)
                .originNodePublicId(originNode != null ? originNode.getPublicId() : null)
                .originNodeName(originNode != null ? originNode.getNodeName() : null)
                .originNodeCode(originNode != null ? originNode.getNodeCode() : null)
                .destinationOrganizationPublicId(destinationNode != null ? destinationNode.getOrganizationPublicId() : null)
                .destinationNodePublicId(destinationNode != null ? destinationNode.getPublicId() : null)
                .destinationNodeName(destinationNode != null ? destinationNode.getNodeName() : null)
                .destinationNodeCode(destinationNode != null ? destinationNode.getNodeCode() : null)
                .currentNodePublicId(currentNode != null ? currentNode.getPublicId() : null)
                .currentNodeName(currentNode != null ? currentNode.getNodeName() : null)
                .currentNodeCode(currentNode != null ? currentNode.getNodeCode() : null)
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .sealedPackagingRequired(shipment.isSealedPackagingRequired())
                .fragile(shipment.isFragile())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
