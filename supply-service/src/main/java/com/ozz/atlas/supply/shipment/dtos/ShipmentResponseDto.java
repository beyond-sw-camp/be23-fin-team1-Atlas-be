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
public class ShipmentResponseDto {

    private Long id;
    private String shipmentNumber;
    private Long poId;
    private Long subPoId;
    private String carrierName;
    private String vehicleNo;
    private String trackingNo;
    private Long originNodeId;
    private Long destinationNodeId;
    private Long currentNodeId;
    private LocalDateTime departureEta;
    private LocalDateTime arrivalEta;
    private LocalDateTime actualDepartedAt;
    private LocalDateTime actualArrivedAt;
    private ShipmentStatus status;
    private boolean temperatureRequired;

    public static ShipmentResponseDto from(Shipment shipment) {
        return ShipmentResponseDto.builder()
                .id(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .poId(shipment.getPoId())
                .subPoId(shipment.getSubPoId())
                .carrierName(shipment.getCarrierName())
                .vehicleNo(shipment.getVehicleNo())
                .trackingNo(shipment.getTrackingNo())
                .originNodeId(shipment.getOriginNodeId())
                .destinationNodeId(shipment.getDestinationNodeId())
                .currentNodeId(shipment.getCurrentNodeId())
                .departureEta(shipment.getDepartureEta())
                .arrivalEta(shipment.getArrivalEta())
                .actualDepartedAt(shipment.getActualDepartedAt())
                .actualArrivedAt(shipment.getActualArrivedAt())
                .status(shipment.getStatus())
                .temperatureRequired(shipment.isTemperatureRequired())
                .build();
    }
}
