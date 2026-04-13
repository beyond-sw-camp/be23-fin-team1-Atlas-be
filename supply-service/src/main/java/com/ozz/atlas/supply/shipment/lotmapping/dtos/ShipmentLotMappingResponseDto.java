package com.ozz.atlas.supply.shipment.lotmapping.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ShipmentLotMappingResponseDto {

    private String shipmentPublicId;
    private String lotPublicId;
    private BigDecimal shippedQty;
    private String unit;
    private LocalDateTime loadedAt;
}
