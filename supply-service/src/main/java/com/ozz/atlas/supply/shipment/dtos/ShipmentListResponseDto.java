package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 목록 응답")
public class ShipmentListResponseDto {

    @Schema(description = "출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789")
    private String publicId;
    @Schema(description = "출하 번호", example = "SHIP-2026-0001")
    private String shipmentNumber;
    @Schema(description = "상위 발주 공개 식별자", example = "po_01HZY1PO123456789", nullable = true)
    private String purchaseOrderPublicId;
    @Schema(description = "하위 발주 공개 식별자", example = "subpo_01HZY1SUBPO123456789", nullable = true)
    private String subPurchaseOrderPublicId;
    @Schema(description = "운송사명", example = "CJ Logistics")
    private String carrierName;
    @Schema(description = "도착 물류 노드 공개 식별자", example = "node_dest_01HZY1BBB")
    private String destinationNodePublicId;
    @Schema(description = "현재 물류 노드 공개 식별자", example = "node_hub_01HZY1CCC", nullable = true)
    private String currentNodePublicId;
    @Schema(description = "예상 도착 시각", example = "2026-04-18T14:00:00")
    private LocalDateTime arrivalEta;
    @Schema(description = "출하 상태", example = "IN_TRANSIT")
    private ShipmentStatus status;

}
