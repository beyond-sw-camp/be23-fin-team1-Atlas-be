package com.ozz.atlas.control.kafka.shipment;

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
        boolean temperatureRequired,
        String rootPurchaseOrderPublicId,
        String rootBuyerOrganizationPublicId,
        String directBuyerOrganizationPublicId,
        String directSupplierOrganizationPublicId,
        String parentPurchaseOrderPublicId
) {
}
