package com.ozz.atlas.control.kafka.shipment;

import java.time.LocalDateTime;

public record ShipmentDelayDetectedPayload(
        String shipmentPublicId,
        String shipmentNumber,
        String status,
        long delayMinutes,
        LocalDateTime arrivalEta,
        LocalDateTime estimatedArrivalAt,
        String currentNodePublicId,
        String rootPurchaseOrderPublicId,
        String rootBuyerOrganizationPublicId,
        String directBuyerOrganizationPublicId,
        String directSupplierOrganizationPublicId,
        String parentPurchaseOrderPublicId,
        String subPurchaseOrderPublicId
) {
}
