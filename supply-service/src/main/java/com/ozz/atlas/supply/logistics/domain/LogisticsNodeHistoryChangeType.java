package com.ozz.atlas.supply.logistics.domain;

import lombok.Getter;

@Getter
public enum LogisticsNodeHistoryChangeType {
    CREATED("거점 생성"),
    UPDATED("거점 정보"),
    CAPACITY_STATUS_CHANGED("가용 상태"),
    ACTIVATED("운영 여부"),
    DEACTIVATED("운영 여부");

    private final String label;

    LogisticsNodeHistoryChangeType(String label) {
        this.label = label;
    }
}
