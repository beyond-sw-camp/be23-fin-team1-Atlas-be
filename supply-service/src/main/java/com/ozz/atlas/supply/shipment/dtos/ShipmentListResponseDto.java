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
public class ShipmentListResponseDto {

    private String publicId;
    private String shipmentNumber;
    private String carrierName;
    private Long destinationNodeId;
    private Long currentNodeId;
    private LocalDateTime arrivalEta;
    private ShipmentStatus status;
}
