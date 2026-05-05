package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 가능 출발 창고 옵션")
public class ShipmentOriginNodeOptionDto {

    private String nodePublicId;
    private String nodeCode;
    private String nodeName;
    private Long availableQty;
}
