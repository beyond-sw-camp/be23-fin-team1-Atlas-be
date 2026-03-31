package com.ozz.atlas.supply.shipment.dtos;

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

    private Long poId;
    private Long subPoId;
    private String carrierName;
    private String vehicleNo;
    private String trackingNo;

    @NotBlank
    private String originNodeId;

    @NotBlank
    private String destinationNodeId;

    @NotNull
    private LocalDateTime departureEta;

    @NotNull
    private LocalDateTime arrivalEta;

    private boolean temperatureRequired;
}
