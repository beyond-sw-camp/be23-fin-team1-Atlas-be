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

    @NotBlank
    @Schema(description = "출발 물류 거점 공개 식별자", example = "node_origin_01HZY1AAA")
    private String originNodePublicId;

    @NotNull
    @Schema(description = "출발 예정 시각", example = "2026-04-18T08:00:00")
    private LocalDateTime departureEta;

    @Schema(description = "냉장/냉동 등 온도 관리 필요 여부", example = "true")
    private boolean temperatureRequired;

    @Schema(description = "밀봉 포장 필요 여부", example = "true")
    private boolean sealedPackagingRequired;

    @Schema(description = "파손 위험 여부", example = "true")
    private boolean fragile;
}
