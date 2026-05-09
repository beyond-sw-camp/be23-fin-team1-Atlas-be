package com.ozz.atlas.supply.purchaseorder.domain;

public enum PurchaseOrderHistoryActionType {
    CREATED("생성"),
    UPDATED("수정"),
    CANCELLED("취소"),
    COMPLETED("완료"),
    DELETED("삭제"),
    REJECTED("거절"),
    ITEM_ADDED("품목 추가"),
    ITEM_UPDATED("품목 수정"),
    ITEM_DELETED("품목 삭제"),
    PARTIALLY_CONFIRMED("일부 승인"),
    CONFIRMED("승인");

    private final String label;

    PurchaseOrderHistoryActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
