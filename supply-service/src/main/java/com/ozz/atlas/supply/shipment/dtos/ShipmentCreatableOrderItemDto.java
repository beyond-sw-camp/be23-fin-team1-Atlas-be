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

    @Schema(description = "발주 품목 라인 공개 식별자", example = "01HZY1ORDERITEM123456789")
    private String sourceItemPublicId;
    @Schema(description = "품목 공개 식별자", example = "01HZY1ITEM12345678901234")
    private String itemPublicId;
    @Schema(description = "품목 코드", example = "ITEM-0001")
    private String itemCode;
    @Schema(description = "품목명", example = "냉장 포장재")
    private String itemName;
    @Schema(description = "대표 미디어 파일 공개 식별자", example = "file_01HZY3FILE123456789")
    private String primaryMediaFilePublicId;
    @Schema(description = "승인 수량", example = "100")
    private Long confirmedQty;
    @Schema(description = "이미 출하 생성된 수량", example = "30")
    private Long alreadyShipmentQty;
    @Schema(description = "추가 출하 가능 수량", example = "70")
    private Long shippableQty;
    @Schema(description = "도착 창고 공개 식별자", example = "01HZY1DESTNODE123456789")
    private String destinationNodePublicId;
    @Schema(description = "도착 창고 코드", example = "WH-BUY1-001")
    private String destinationNodeCode;
    @Schema(description = "도착 창고명", example = "발주사 인천 창고")
    private String destinationNodeName;
    @Schema(description = "출발 창고 후보 목록")
    private List<ShipmentOriginNodeOptionDto> originNodeOptions;
}
