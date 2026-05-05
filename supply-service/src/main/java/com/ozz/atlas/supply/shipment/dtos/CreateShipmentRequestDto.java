package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 생성 요청")
public class CreateShipmentRequestDto {

    @Schema(description = "상위 발주 ID", example = "101")
    private Long poId;

    @Schema(description = "상위 발주 공개 식별자", example = "01HZY1PO1234567890123456")
    private String purchaseOrderPublicId;

    @Schema(description = "하위 발주 ID", example = "202", nullable = true)
    private Long subPoId;

    @Schema(description = "하위 발주 공개 식별자", example = "01HZY1SUBPO123456789012", nullable = true)
    private String subPurchaseOrderPublicId;

    @Schema(description = "출발 창고 공개 식별자. 품목 라인 기반 생성에서는 자동 계산됩니다.", example = "01HZY1NODE12345678901234")
    private String originNodePublicId;

    @Schema(description = "출하 생성 품목 목록")
    private List<CreateShipmentLineRequestDto> shipmentLines;

    @NotNull
    @Schema(description = "출발 예정 시각", example = "2026-04-18T08:00:00")
    private LocalDateTime departureEta;

    @Schema(description = "온도 관리 필요 여부", example = "true")
    private boolean temperatureRequired;

    @Schema(description = "밀봉 포장 필요 여부", example = "true")
    private boolean sealedPackagingRequired;

    @Schema(description = "파손 위험 여부", example = "true")
    private boolean fragile;
}
