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

    @Schema(description = "출하 기준 유형", example = "ORDER")
    private ShipmentSourceType sourceType;
    @Schema(description = "출하 기준 공개 식별자", example = "01HZY1ORDER1234567890123")
    private String sourcePublicId;
    @Schema(description = "발주 번호", example = "PO-2026-0000009")
    private String orderNumber;
    @Schema(description = "발주 조직 공개 식별자", example = "01HQBUYER789ABCDEF01HQ")
    private String buyerOrganizationPublicId;
    @Schema(description = "공급사 공개 식별자", example = "01HQSPL789ABCDEF01HQSPL")
    private String supplierPublicId;
    @Schema(description = "공급사명", example = "오발조")
    private String supplierName;
    @Schema(description = "발주 승인 상태", example = "ACCEPTED")
    private String status;
    @Schema(description = "출하 생성 가능 품목 목록")
    private List<ShipmentCreatableOrderItemDto> items;
}
