package com.ozz.atlas.control.kafka.shipment;

import java.time.LocalDateTime;

public record ShipmentDelayDetectedPayload(
        String shipmentPublicId,
        String shipmentNumber,
        String status,
        long delayMinutes,
        LocalDateTime arrivalEta,
        LocalDateTime estimatedArrivalAt,
        String currentNodePublicId
) {
}
