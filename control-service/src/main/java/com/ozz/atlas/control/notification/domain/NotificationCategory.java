package com.ozz.atlas.control.notification.domain;

import com.ozz.atlas.common.kafka.EventTypes;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {
    PURCHASE_ORDER("발주", "발주 생성, 수정, 확정, 수락, 거절, 취소 알림", 1, true),
    SUB_PURCHASE_ORDER("하위 발주", "하위 발주 생성, 확정, 거절, 취소 알림", 2, true),
    SHIPMENT("출하/배송", "출하 생성, 출발, 도착, 완료, 지연 감지 알림", 3, true),
    DELIVERY_EXCEPTION("배송 예외", "배송 지연, 온도 이탈, 파손 등 배송 예외 알림", 4, true),
    LOGISTICS_NODE("물류 거점", "창고 및 물류 거점 상태 변경 알림", 5, true),
    INVENTORY("재고", "재고 부족 등 재고 상태 알림", 6, true),
    RETURN_REQUEST("반품", "반품 요청 생성, 승인, 거절, 완료, 취소 알림", 8, true),
    SUPPLIER_CERTIFICATE("협력사 인증서", "협력사 인증서 생성, 승인, 거절, 만료 알림", 9, true),
    SUPPLIER_RISK("협력사 리스크", "협력사 점수 급락, ESG 위반 등 리스크 알림", 10, true),
    RECOMMENDATION("AI 권고안", "AI 권고안 생성, 실패, 수락, 거절 알림", 11, true),
    SYSTEM("시스템", "시스템 운영 알림", 12, true);

    private final String label;
    private final String description;
    private final int displayOrder;
    private final boolean userConfigurable;

    public static Optional<NotificationCategory> fromEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }

        if (eventType.startsWith("purchase-order.")) {
            return Optional.of(PURCHASE_ORDER);
        }
        if (eventType.startsWith("sub-purchase-order.")) {
            return Optional.of(SUB_PURCHASE_ORDER);
        }
        if (eventType.startsWith("shipment.")) {
            return Optional.of(SHIPMENT);
        }
        if (eventType.startsWith("delivery-exception.")) {
            return Optional.of(DELIVERY_EXCEPTION);
        }
        if (eventType.startsWith("logistics-node.")) {
            return Optional.of(LOGISTICS_NODE);
        }
        if (eventType.startsWith("inventory.")) {
            return Optional.of(INVENTORY);
        }
        if (eventType.startsWith("return-request.")) {
            return Optional.of(RETURN_REQUEST);
        }
        if (eventType.startsWith("supplier-certificate.")) {
            return Optional.of(SUPPLIER_CERTIFICATE);
        }
        if (eventType.startsWith("supplier.")) {
            return Optional.of(SUPPLIER_RISK);
        }
        if (isRecommendationEvent(eventType)) {
            return Optional.of(RECOMMENDATION);
        }
        return Optional.empty();
    }

    public static boolean exists(String category) {
        return Arrays.stream(values()).anyMatch(value -> value.name().equals(category));
    }

    private static boolean isRecommendationEvent(String eventType) {
        return EventTypes.RECOMMENDATION_REQUESTED.equals(eventType)
                || EventTypes.RECOMMENDATION_GENERATED.equals(eventType)
                || EventTypes.RECOMMENDATION_FAILED.equals(eventType)
                || EventTypes.RECOMMENDATION_ACCEPTED.equals(eventType)
                || EventTypes.RECOMMENDATION_REJECTED.equals(eventType);
    }
}
