package com.ozz.atlas.common.event;

public final class EventTypes {

    public static final String PURCHASE_ORDER_CREATED = "purchase-order.created";
    public static final String PURCHASE_ORDER_STATUS_CHANGED = "purchase-order.status-changed";

    public static final String SHIPMENT_CREATED = "shipment.created";
    public static final String SHIPMENT_DEPARTED = "shipment.departed";
    public static final String SHIPMENT_COMPLETED = "shipment.completed";
    public static final String SHIPMENT_DELAY_DETECTED = "shipment.delay-detected";

    public static final String DELIVERY_EXCEPTION_CREATED = "delivery-exception.created";

    public static final String LOT_CREATED = "lot.created";
    public static final String LOT_STATUS_CHANGED = "lot.status-changed";
    public static final String LOT_QUALITY_STATUS_CHANGED = "lot.quality-status-changed";

    public static final String SUPPLIER_CERTIFICATE_STATUS_CHANGED = "supplier-certificate.status-changed";
    public static final String SUPPLIER_CERTIFICATE_EXPIRING = "supplier-certificate.expiring";

    public static final String RECOMMENDATION_REQUESTED = "recommendation.requested";
    public static final String RECOMMENDATION_GENERATED = "recommendation.generated";
    public static final String RECOMMENDATION_FAILED = "recommendation.failed";

    private EventTypes() {
    }
}
