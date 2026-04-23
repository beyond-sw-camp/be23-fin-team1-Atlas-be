package com.ozz.atlas.common.kafka;

public final class EventTypes {

    public static final String PURCHASE_ORDER_CREATED = "purchase-order.created";
    public static final String PURCHASE_ORDER_UPDATED = "purchase-order.updated";
    public static final String PURCHASE_ORDER_CONFIRMED = "purchase-order.confirmed";
    public static final String PURCHASE_ORDER_ACCEPTED = "purchase-order.accepted";
    public static final String PURCHASE_ORDER_REJECTED = "purchase-order.rejected";
    public static final String PURCHASE_ORDER_CANCELLED = "purchase-order.cancelled";

    public static final String SHIPMENT_CREATED = "shipment.created";
    public static final String SHIPMENT_DEPARTED = "shipment.departed";
    public static final String SHIPMENT_ARRIVED = "shipment.arrived";
    public static final String SHIPMENT_COMPLETED = "shipment.completed";
    public static final String SHIPMENT_DELAY_DETECTED = "shipment.delay-detected";

    public static final String DELIVERY_EXCEPTION_CREATED = "delivery-exception.created";
    public static final String DELIVERY_EXCEPTION_DELAY = "delivery-exception.delay";
    public static final String DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION = "delivery-exception.temperature-deviation";
    public static final String DELIVERY_EXCEPTION_DAMAGED = "delivery-exception.damaged";

    public static final String LOGISTICS_NODE_CAPACITY_STATUS_CHANGED = "logistics-node.capacity-status-changed";
    public static final String INVENTORY_SHORTAGE_DETECTED = "inventory.shortage-detected";

    public static final String LOT_CREATED = "lot.created";
    public static final String LOT_IN_PRODUCTION = "lot.in-production";
    public static final String LOT_COMPLETED = "lot.completed";
    public static final String LOT_HOLD = "lot.hold";
    public static final String LOT_RELEASED = "lot.released";
    public static final String LOT_DEFECTIVE = "lot.defective";
    public static final String LOT_EXPIRATION_IMMINENT = "lot.expiration-imminent";
    public static final String LOT_QUALITY_PASSED = "lot.quality-passed";
    public static final String LOT_QUALITY_FAILED = "lot.quality-failed";

    public static final String RETURN_REQUEST_CREATED = "return-request.created";
    public static final String RETURN_REQUEST_APPROVED = "return-request.approved";
    public static final String RETURN_REQUEST_REJECTED = "return-request.rejected";
    public static final String RETURN_REQUEST_COMPLETED = "return-request.completed";
    public static final String RETURN_REQUEST_CANCELLED = "return-request.cancelled";

    public static final String SUPPLIER_CERTIFICATE_CREATED = "supplier-certificate.created";
    public static final String SUPPLIER_CERTIFICATE_APPROVED = "supplier-certificate.approved";
    public static final String SUPPLIER_CERTIFICATE_REJECTED = "supplier-certificate.rejected";
    public static final String SUPPLIER_CERTIFICATE_EXPIRING = "supplier-certificate.expiring";
    public static final String SUPPLIER_CERTIFICATE_EXPIRED = "supplier-certificate.expired";
    public static final String SUPPLIER_CERTIFICATE_REVOKED = "supplier-certificate.revoked";

    public static final String SUPPLIER_SCORE_DROPPED = "supplier.score-dropped";
    public static final String SUPPLIER_ESG_VIOLATED = "supplier.esg-violated";

    public static final String RECOMMENDATION_REQUESTED = "recommendation.requested";
    public static final String RECOMMENDATION_GENERATED = "recommendation.generated";
    public static final String RECOMMENDATION_FAILED = "recommendation.failed";
    public static final String RECOMMENDATION_ACCEPTED = "recommendation.accepted";
    public static final String RECOMMENDATION_REJECTED = "recommendation.rejected";

    private EventTypes() {
    }
}
