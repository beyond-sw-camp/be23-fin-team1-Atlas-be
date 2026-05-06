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

    @Schema(description = "출발 창고 공개 식별자", example = "01HZY1ORIGINNODE12345678")
    private String nodePublicId;
    @Schema(description = "출발 창고 코드", example = "WH-CHO1-003")
    private String nodeCode;
    @Schema(description = "출발 창고명", example = "서울 물류창고")
    private String nodeName;
    @Schema(description = "해당 창고에서 출하 가능한 수량", example = "50")
    private Long availableQty;
}
