package com.ozz.atlas.supply.purchaseorder.domain;

public enum PurchaseOrderItemStatus {
    OPEN,                 // 발주 생성 직후
    PARTIALLY_CONFIRMED,  // 일부 수량만 확정
    CONFIRMED,            // 전체 수량 확정
    REJECTED,             // 발주 자체 거절에 의해 반영
    CANCELLED,            // 발주 취소에 의해 반영
    DELETED               // 상세 항목 삭제
}
