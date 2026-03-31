package com.ozz.atlas.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전사 공통 도메인 유형 (알림, 채팅 참조 카드, 리스크 등에서 공통 사용)
 */
@Getter
@RequiredArgsConstructor
public enum DomainType {
    // 알림 및 참조 공통
    RISK("리스크"),
    TASK("업무/태스크"),
    CHAT("채팅"),
    SYSTEM("시스템"),

    // 공급망 실행 참조 전용
    ORDER("발주"),
    SHIPMENT("출하"),
    LOT("LOT"),
    SUPPLIER("협력사"),
    ITEM("품목");

    private final String description;
}
