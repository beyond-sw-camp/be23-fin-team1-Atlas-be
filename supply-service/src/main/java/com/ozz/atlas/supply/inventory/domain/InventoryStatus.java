package com.ozz.atlas.supply.inventory.domain;

public enum InventoryStatus {
    ACTIVE, // 정상/일부 예약 재고
    RESERVED, // 예약 재고
    EXHAUSTED, // 소진 재고
    EXPIRED, // 유통기한 만료 재고
    DELETED // 삭제/폐기 재고
}
