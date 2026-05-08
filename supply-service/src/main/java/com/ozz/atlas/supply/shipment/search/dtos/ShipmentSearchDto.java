package com.ozz.atlas.supply.shipment.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Shipment 값 검색 조건")
public class ShipmentSearchDto {

    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;

    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ShipmentStatus status; //배송상태

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String originNodePublicId; //출발지

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String destinationNodePublicId; //도착지

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String currentNodePublicId; //현재위치

    @Schema(description = "temperature Required 값", example = "true", nullable = true)
    private Boolean temperatureRequired; //온도관리 여부
}
