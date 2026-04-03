package com.ozz.atlas.supply.shipment.dtos;

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
public class ShipmentStatusHistoryResponseDto {

    private Long id;
    private Long shipmentId;
    private ShipmentStatus statusCode;
    private String statusMessage;
    private String locationText;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime recordedAt;
    private String recordedBy;

    public static ShipmentStatusHistoryResponseDto from(ShipmentStatusHistory history){
        return ShipmentStatusHistoryResponseDto.builder()
                .id(history.getId())
                .shipmentId(history.getShipmentId())
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
