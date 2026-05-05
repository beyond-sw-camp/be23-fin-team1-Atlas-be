package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 품목 라인 응답")
public class ShipmentLineResponseDto {

    @Schema(description = "출하 품목 라인 공개 식별자", example = "01HZY1SHIPLINE123456789012")
    private String publicId;

    @Schema(description = "출하 유형", example = "ORDER")
    private ShipmentSourceType sourceType;

    @Schema(description = "출하 기준 공개 식별자", example = "01HZY1SOURCE12345678901234")
    private String sourcePublicId;

    @Schema(description = "출하 기준 품목 공개 식별자", example = "01HZY1SOURCEITEM1234567890")
    private String sourceItemPublicId;

    @Schema(description = "품목 공개 식별자", example = "01HZY1ITEM123456789012345")
    private String itemPublicId;

    @Schema(description = "품목 코드", example = "ITEM-001")
    private String itemCode;

    @Schema(description = "품목명", example = "냉장 샐러드")
    private String itemName;

    @Schema(description = "출하 수량", example = "100")
    private Long quantity;

    @Schema(description = "출발 창고 내부 ID", example = "1")
    private Long originNodeId;

    @Schema(description = "도착 창고 내부 ID", example = "2")
    private Long destinationNodeId;

    public static ShipmentLineResponseDto from(ShipmentLine shipmentLine) {
        return ShipmentLineResponseDto.builder()
                .publicId(shipmentLine.getPublicId())
                .sourceType(shipmentLine.getSourceType())
                .sourcePublicId(shipmentLine.getSourcePublicId())
                .sourceItemPublicId(shipmentLine.getSourceItemPublicId())
                .itemPublicId(shipmentLine.getItemPublicId())
                .itemCode(shipmentLine.getItemCode())
                .itemName(shipmentLine.getItemName())
                .quantity(shipmentLine.getQuantity())
                .originNodeId(shipmentLine.getOriginNodeId())
                .destinationNodeId(shipmentLine.getDestinationNodeId())
                .build();
    }
}
