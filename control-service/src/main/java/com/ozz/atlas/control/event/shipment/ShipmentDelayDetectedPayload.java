package com.ozz.atlas.control.event.shipment;

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
