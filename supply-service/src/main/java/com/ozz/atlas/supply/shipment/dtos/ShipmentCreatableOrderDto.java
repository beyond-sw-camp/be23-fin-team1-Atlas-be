package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
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
@Schema(description = "출하 생성 가능 발주")
public class ShipmentCreatableOrderDto {

    private ShipmentSourceType sourceType;
    private String sourcePublicId;
    private String orderNumber;
    private String buyerOrganizationPublicId;
    private String supplierPublicId;
    private String supplierName;
    private String status;
    private List<ShipmentCreatableOrderItemDto> items;
}
