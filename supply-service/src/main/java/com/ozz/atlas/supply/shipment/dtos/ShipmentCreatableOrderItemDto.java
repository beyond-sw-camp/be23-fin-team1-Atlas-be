package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 생성 가능 발주 품목")
public class ShipmentCreatableOrderItemDto {

    private String sourceItemPublicId;
    private String itemPublicId;
    private String itemCode;
    private String itemName;
    private Long confirmedQty;
    private Long alreadyShipmentQty;
    private Long shippableQty;
    private String destinationNodePublicId;
    private String destinationNodeCode;
    private String destinationNodeName;
    private List<ShipmentOriginNodeOptionDto> originNodeOptions;
}
