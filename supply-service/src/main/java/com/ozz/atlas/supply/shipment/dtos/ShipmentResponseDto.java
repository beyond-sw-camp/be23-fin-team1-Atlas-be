package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShipmentResponseDto {

    private String publicId;
    private String shipmentNumber;
    private Long poId;
    private Long subPoId;
    private String carrierName;
    private String vehicleNo;
    private String trackingNo;
    private String originNodeId;
    private String destinationNodeId;
    private String currentNodeId;
    private LocalDateTime departureEta;
    private LocalDateTime arrivalEta;
    private LocalDateTime actualDepartedAt;
    private LocalDateTime actualArrivedAt;
    private ShipmentStatus status;
    private boolean temperatureRequired;
}
