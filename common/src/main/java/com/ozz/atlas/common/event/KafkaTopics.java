package com.ozz.atlas.common.event;

public final class KafkaTopics {

    public static final String SUPPLY_PURCHASE_ORDER = "atlas.supply.purchase-order";
    public static final String SUPPLY_SHIPMENT = "atlas.supply.shipment";
    public static final String SUPPLY_DELIVERY_EXCEPTION = "atlas.supply.delivery-exception";
    public static final String SUPPLY_LOT = "atlas.supply.lot";
    public static final String SUPPLY_SUPPLIER_CERTIFICATE = "atlas.supply.supplier-certificate";

    public static final String CONTROL_RECOMMENDATION_REQUESTED = "atlas.control.recommendation-requested";
    public static final String CONTROL_RECOMMENDATION_GENERATED = "atlas.control.recommendation-generated";
    public static final String CONTROL_RECOMMENDATION_FAILED = "atlas.control.recommendation-failed";

    private KafkaTopics() {
    }
}
