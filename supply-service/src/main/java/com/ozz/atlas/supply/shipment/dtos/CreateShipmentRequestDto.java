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

    @NotNull
    private Long poId;

    private Long subPoId;

    @NotBlank
    private String carrierName;

    @NotBlank
    private String vehicleNo;

    @NotBlank
    private String trackingNo;

    @NotBlank
    private String originNodePublicId;

    @NotBlank
    private String destinationNodePublicId;

    @NotNull
    private LocalDateTime departureEta;

    @NotNull
    private LocalDateTime arrivalEta;

    private boolean temperatureRequired;

}
