package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.DeliveryException;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionSeverity;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeliveryExceptionResponseDto {

    private String shipmentPublicId;
    private DeliveryExceptionType exceptionType;
    private DeliveryExceptionSeverity severity;
    private LocalDateTime detectedAt;
    private boolean resolved;
    private LocalDateTime resolvedAt;
    private String note;

    public static DeliveryExceptionResponseDto from(DeliveryException deliveryException, String shipmentPublicId) {
        return DeliveryExceptionResponseDto.builder()
                .shipmentPublicId(shipmentPublicId)
                .exceptionType(deliveryException.getExceptionType())
                .severity(deliveryException.getSeverity())
                .detectedAt(deliveryException.getDetectedAt())
                .resolved(deliveryException.isResolved())
                .resolvedAt(deliveryException.getResolvedAt())
                .note(deliveryException.getNote())
                .build();
    }
}
