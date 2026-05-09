package com.ozz.atlas.supply.inventory.domain;

public enum SupplyItemInventoryHistoryActionType {
    CREATED("재고 생성"),
    UPDATED("재고 수정"),
    DELETED("재고 삭제"),
    RESERVED("재고 예약"),
    RELEASED("예약 해제"),
    SHIPMENT_DEDUCTED("출하 차감"),
    RETURN_RESTOCKED("반품 입고"),
    DEFECTIVE_ADDED("불량 입고"),
    DEFECTIVE_DEDUCTED("불량 차감"),
    ADJUSTMENT("재고 조정");

    private final String label;

    SupplyItemInventoryHistoryActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
