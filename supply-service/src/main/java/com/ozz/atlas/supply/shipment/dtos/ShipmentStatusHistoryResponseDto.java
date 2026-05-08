package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "상태 응답")
public class ShipmentStatusHistoryResponseDto {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String shipmentPublicId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ShipmentStatus statusCode;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String statusMessage;
    @Schema(description = "location Text 값", example = "sample", nullable = true)
    private String locationText;
    @Schema(description = "latitude 값", example = "1", nullable = true)
    private BigDecimal latitude;
    @Schema(description = "longitude 값", example = "1", nullable = true)
    private BigDecimal longitude;
    @Schema(description = "recorded At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime recordedAt;
    @Schema(description = "recorded By 값", example = "sample", nullable = true)
    private String recordedBy;
    public static ShipmentStatusHistoryResponseDto from(ShipmentStatusHistory history, String shipmentPublicId){
        return ShipmentStatusHistoryResponseDto.builder()
                .shipmentPublicId(shipmentPublicId)
                .statusCode(history.getStatusCode())
                .statusMessage(history.getStatusMessage())
                .locationText(history.getLocationText())
                .latitude(history.getLatitude())
                .longitude(history.getLongitude())
                .recordedAt(history.getRecordedAt())
                .recordedBy(history.getRecordedBy())
                .build();
    }
}
