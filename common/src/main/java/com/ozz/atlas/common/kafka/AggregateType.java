package com.ozz.atlas.common.kafka;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 aggregate 유형")
public enum AggregateType {
    PURCHASE_ORDER,
    SUB_PURCHASE_ORDER,
    SHIPMENT,
    DELIVERY_EXCEPTION,
    LOGISTICS_NODE,
    INVENTORY,
    RETURN_REQUEST,
    SUPPLIER_CERTIFICATE,
    RISK,
    RECOMMENDATION
}
