package com.ozz.atlas.common.kafka;

public final class KafkaTopics {

    public static final String SUPPLY_PURCHASE_ORDER = "atlas.supply.purchase-order";
    public static final String SUPPLY_SUB_PURCHASE_ORDER = "atlas.supply.sub-purchase-order";
    public static final String SUPPLY_SHIPMENT = "atlas.supply.shipment";
    public static final String SUPPLY_DELIVERY_EXCEPTION = "atlas.supply.delivery-exception";
    public static final String SUPPLY_LOGISTICS_NODE = "atlas.supply.logistics-node";
    public static final String SUPPLY_INVENTORY = "atlas.supply.inventory";
    public static final String SUPPLY_LOT = "atlas.supply.lot";
    public static final String SUPPLY_RETURN_REQUEST = "atlas.supply.return-request";
    public static final String SUPPLY_SUPPLIER_RISK = "atlas.supply.supplier-risk";
    public static final String SUPPLY_SUPPLIER_CERTIFICATE = "atlas.supply.supplier-certificate";

    public static final String CONTROL_RECOMMENDATION_REQUESTED = "atlas.control.recommendation-requested";
    public static final String CONTROL_RECOMMENDATION_GENERATED = "atlas.control.recommendation-generated";
    public static final String CONTROL_RECOMMENDATION_FAILED = "atlas.control.recommendation-failed";
    public static final String CONTROL_RECOMMENDATION_DECISION = "atlas.control.recommendation-decision";

    private KafkaTopics() {
    }
}
