package com.ozz.atlas.supply.purchaseorder.domain;

public enum PurchaseOrderItemStatus {
    OPEN, // 생성됨 (초기)
    PARTIALLY_CONFIRMED, // 일부 입고
    CONFIRMED, // 확정됨
    CANCELLED // 취소
}
