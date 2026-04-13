package com.ozz.atlas.supply.shipment.lotmapping.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateShipmentLotMappingRequestDto {

    @NotBlank
    private String lotPublicId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal shippedQty;

}
