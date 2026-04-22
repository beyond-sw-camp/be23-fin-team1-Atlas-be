package com.ozz.atlas.common.kafka;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 aggregate 유형")
public enum AggregateType {
    PURCHASE_ORDER,
    SHIPMENT,
    DELIVERY_EXCEPTION,
    LOT,
    RETURN_REQUEST,
    SUPPLIER_CERTIFICATE,
    RISK,
    RECOMMENDATION
}
