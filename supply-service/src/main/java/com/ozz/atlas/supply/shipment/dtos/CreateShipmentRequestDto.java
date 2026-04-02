package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateShipmentRequestDto {

    @NotBlank
    private String shipmentNumber;

    @NotNull
    private Long poId;

    private Long subPoId;

    @NotBlank
    private String carrierName;

    @NotBlank
    private String vehicleNo;

    @NotBlank
    private String trackingNo;

    @NotNull
    private Long originNodeId;

    @NotNull
    private Long destinationNodeId;

    @NotNull
    private LocalDateTime departureEta;

    @NotNull
    private LocalDateTime arrivalEta;

    private boolean temperatureRequired;

    public Shipment toEntity() {
        return Shipment.builder()
                .shipmentNumber(this.shipmentNumber)
                .poId(this.poId)
                .subPoId(this.subPoId)
                .carrierName(this.carrierName)
                .vehicleNo(this.vehicleNo)
                .trackingNo(this.trackingNo)
                .originNodeId(this.originNodeId)
                .destinationNodeId(this.destinationNodeId)
                .currentNodeId(this.originNodeId)
                .departureEta(this.departureEta)
                .arrivalEta(this.arrivalEta)
                .status(ShipmentStatus.READY)
                .temperatureRequired(this.temperatureRequired)
                .build();
    }
}
