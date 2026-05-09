package com.ozz.atlas.supply.item.domain;

public enum SupplyItemHistoryActionType {
    CREATED("생성"),
    UPDATED("수정"),
    DELETED("삭제"),
    ACTIVATED("활성화"),
    DEACTIVATED("비활성화"),
    SUPPLY_TYPE_CHANGED("공급 유형 변경"),
    PRIMARY_MEDIA_CHANGED("대표 미디어 변경"),
    MEDIA_CHANGED("미디어 수정");

    private final String label;

    SupplyItemHistoryActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
