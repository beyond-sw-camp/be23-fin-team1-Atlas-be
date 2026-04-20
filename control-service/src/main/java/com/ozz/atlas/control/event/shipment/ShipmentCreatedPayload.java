package com.ozz.atlas.control.event.shipment;

import java.time.LocalDateTime;

public record ShipmentCreatedPayload(
        String shipmentPublicId,
        String shipmentNumber,
        String purchaseOrderPublicId,
        String subPurchaseOrderPublicId,
        String originNodePublicId,
        String destinationNodePublicId,
        LocalDateTime departureEta,
        LocalDateTime arrivalEta,
        String status,
        boolean temperatureRequired
) {
}
