package com.ozz.atlas.supply.supplier.domain;

public enum SupplierTierLevel {
    TIER1(1),
    TIER2(2),
    TIER3(3);

    private final int order;

    SupplierTierLevel(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public SupplierTierLevel next() {
        return switch (this) {
            case TIER1 -> TIER2;
            case TIER2 -> TIER3;
            case TIER3 -> null;
        };
    }
}
