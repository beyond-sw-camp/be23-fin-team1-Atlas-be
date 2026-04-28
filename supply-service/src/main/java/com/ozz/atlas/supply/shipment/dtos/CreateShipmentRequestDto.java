package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 생성 요청")
public class CreateShipmentRequestDto {

    @Schema(description = "상위 발주 ID", example = "101")
    private Long poId;

    @Schema(description = "상위 발주 공개 식별자", example = "po_01HZY1PO123456789")
    private String purchaseOrderPublicId;

    @Schema(description = "하위 발주 ID", example = "202", nullable = true)
    private Long subPoId;

    @Schema(description = "하위 발주 공개 식별자", example = "subpo_01HZY1SUBPO123456789", nullable = true)
    private String subPurchaseOrderPublicId;

    @Schema(description = "운송사명", example = "CJ Logistics", nullable = true)
    private String carrierName;

    @Schema(description = "차량 번호", example = "12가3456", nullable = true)
    private String vehicleNo;

    @Schema(description = "운송 추적 번호", example = "TRK-ATLAS-20260417", nullable = true)
    private String trackingNo;

    @NotBlank
    @Schema(description = "출발 물류 노드 공개 식별자", example = "node_origin_01HZY1AAA")
    private String originNodePublicId;

    @NotBlank
    @Schema(description = "도착 물류 노드 공개 식별자", example = "node_dest_01HZY1BBB")
    private String destinationNodePublicId;

    @NotNull
    @Schema(description = "예상 출발 시각", example = "2026-04-18T08:00:00")
    private LocalDateTime departureEta;

    @NotNull
    @Schema(description = "예상 도착 시각", example = "2026-04-18T14:00:00")
    private LocalDateTime arrivalEta;

    @Schema(description = "냉장/냉동 등 온도 관리 필요 여부", example = "true")
    private boolean temperatureRequired;

}
