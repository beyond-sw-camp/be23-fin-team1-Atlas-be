package com.ozz.atlas.supply.kafka.context;

public record SupplyChainContext(
        String rootPurchaseOrderPublicId,
        String rootBuyerOrganizationPublicId,
        String directBuyerOrganizationPublicId,
        String directSupplierOrganizationPublicId,
        String parentPurchaseOrderPublicId,
        String subPurchaseOrderPublicId
) {

    public static SupplyChainContext empty() {
        return new SupplyChainContext(null, null, null, null, null, null);
    }
}
