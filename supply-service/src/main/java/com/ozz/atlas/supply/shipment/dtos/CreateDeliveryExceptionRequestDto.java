package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionSeverity;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateDeliveryExceptionRequestDto {

    @NotBlank
    private String shipmentPublicId;

    @NotNull
    private DeliveryExceptionType exceptionType;

    @NotNull
    private DeliveryExceptionSeverity severity;

    @NotNull
    private LocalDateTime detectedAt;

    private String note;
}
