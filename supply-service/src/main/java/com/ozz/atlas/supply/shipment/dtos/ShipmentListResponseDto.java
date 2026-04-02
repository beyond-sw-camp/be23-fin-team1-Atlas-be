package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.Shipment;
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

    private Long id;
    private String shipmentNumber;
    private String carrierName;
    private Long destinationNodeId;
    private Long currentNodeId;
    private LocalDateTime arrivalEta;
    private ShipmentStatus status;

    public static ShipmentListResponseDto from(Shipment shipment) {
        return ShipmentListResponseDto.builder()
                .id(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .carrierName(shipment.getCarrierName())
                .destinationNodeId(shipment.getDestinationNodeId())
                .currentNodeId(shipment.getCurrentNodeId())
                .arrivalEta(shipment.getArrivalEta())
                .status(shipment.getStatus())
                .build();
    }
}
