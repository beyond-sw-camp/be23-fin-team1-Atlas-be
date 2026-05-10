package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.control.kafka.recommendation.RecommendationDecisionPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationFailedPayload;
import com.ozz.atlas.control.kafka.recommendation.RecommendationGeneratedPayload;
import com.ozz.atlas.control.notification.command.NotificationCommand;
import com.ozz.atlas.control.notification.domain.NotificationToastType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationCommandFactory {

    private final ObjectMapper objectMapper;

    public List<NotificationCommand> create(
            EventEnvelope<JsonNode> eventEnvelope,
            List<String> recipientUserPublicIds
    ) {
        if (recipientUserPublicIds == null || recipientUserPublicIds.isEmpty()) {
            return List.of();
        }

        return recipientUserPublicIds.stream()
                .map(recipientUserPublicId -> createCommand(eventEnvelope, recipientUserPublicId))
                .toList();
    }

    private NotificationCommand createCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        return switch (eventEnvelope.eventType()) {
            case EventTypes.RECOMMENDATION_GENERATED ->
                    generatedCommand(eventEnvelope, recipientUserPublicId);
            case EventTypes.RECOMMENDATION_FAILED ->
                    failedCommand(eventEnvelope, recipientUserPublicId);
            case EventTypes.RECOMMENDATION_ACCEPTED,
                 EventTypes.RECOMMENDATION_REJECTED ->
                    decisionCommand(eventEnvelope, recipientUserPublicId);
            default -> supplyCommand(eventEnvelope, recipientUserPublicId);
        };
    }

    private NotificationCommand generatedCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationGeneratedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationGeneratedPayload.class);

        String targetLabel = StringUtils.hasText(payload.shipmentPublicId())
                ? payload.shipmentPublicId() + " 출하"
                : payload.riskType() + " 리스크";

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .eventType(eventEnvelope.eventType())
                .domainType(DomainType.RISK)
                .toastType(NotificationToastType.SUCCESS)
                .title("권고안 생성이 완료되었습니다.")
                .message(targetLabel + "에 대한 권고안이 생성되었습니다.")
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }

    private NotificationCommand failedCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationFailedPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationFailedPayload.class);

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .eventType(eventEnvelope.eventType())
                .domainType(DomainType.SYSTEM)
                .toastType(NotificationToastType.RISK_ALERT)
                .title("권고안 생성에 실패했습니다.")
                .message(payload.errorMessage())
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }

    private NotificationCommand decisionCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        RecommendationDecisionPayload payload =
                objectMapper.convertValue(eventEnvelope.payload(), RecommendationDecisionPayload.class);

        String title = EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                ? "권고안이 수락되었습니다."
                : "권고안이 거절되었습니다.";
        String message = EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                ? payload.shipmentPublicId() + " 출하 대응 권고안이 수락되었습니다."
                : payload.shipmentPublicId() + " 출하 대응 권고안이 거절되었습니다.";

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .eventType(eventEnvelope.eventType())
                .domainType(DomainType.RISK)
                .toastType(EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                        ? NotificationToastType.SUCCESS
                        : NotificationToastType.WARNING)
                .title(title)
                .message(message)
                .deepLinkUrl("/recommendations/" + payload.recommendationPublicId())
                .referencePublicId(payload.recommendationPublicId())
                .build();
    }

    private NotificationCommand supplyCommand(
            EventEnvelope<JsonNode> eventEnvelope,
            String recipientUserPublicId
    ) {
        JsonNode payload = eventEnvelope.payload();
        String eventName = StringUtils.hasText(text(payload, "eventName"))
                ? text(payload, "eventName")
                : resolveEventName(eventEnvelope.eventType());
        String referenceNumber = text(payload, "referenceNumber");
        String description = StringUtils.hasText(text(payload, "description"))
                ? text(payload, "description")
                : resolveEventDescription(eventEnvelope.eventType());
        String referencePublicId = StringUtils.hasText(text(payload, "referencePublicId"))
                ? text(payload, "referencePublicId")
                : eventEnvelope.aggregatePublicId();

        boolean useReferenceNumberAsTitle = isPurchaseOrderCreated(eventEnvelope.eventType())
                && StringUtils.hasText(referenceNumber);
        String title = useReferenceNumberAsTitle
                ? referenceNumber
                : StringUtils.hasText(eventName)
                ? eventName
                : "공급망 이벤트가 발생했습니다.";
        String messageTarget = StringUtils.hasText(referenceNumber) ? referenceNumber : referencePublicId;
        String message = StringUtils.hasText(description)
                ? buildSupplyMessage(eventEnvelope.eventType(), messageTarget, description)
                : messageTarget + " 이벤트가 발생했습니다.";

        return NotificationCommand.builder()
                .recipientUserPublicId(recipientUserPublicId)
                .eventType(eventEnvelope.eventType())
                .domainType(resolveDomainType(eventEnvelope.eventType()))
                .toastType(resolveToastType(eventEnvelope.eventType()))
                .title(title)
                .message(message)
                .deepLinkUrl(resolveDeepLink(eventEnvelope.eventType(), referencePublicId))
                .referencePublicId(referencePublicId)
                .build();
    }

    private boolean isPurchaseOrderCreated(String eventType) {
        return EventTypes.PURCHASE_ORDER_CREATED.equals(eventType)
                || EventTypes.SUB_PURCHASE_ORDER_CREATED.equals(eventType);
    }

    private String buildSupplyMessage(String eventType, String messageTarget, String description) {
        String sentence = resolveSupplyMessageSentence(eventType, description);
        if (StringUtils.hasText(messageTarget)) {
            return messageTarget + " " + sentence;
        }
        return sentence;
    }

    private String resolveSupplyMessageSentence(String eventType, String fallbackDescription) {
        return switch (eventType) {
            case EventTypes.PURCHASE_ORDER_CREATED -> "신규 발주가 생성되었습니다.";
            case EventTypes.PURCHASE_ORDER_UPDATED -> "발주가 수정되었습니다.";
            case EventTypes.PURCHASE_ORDER_CONFIRMED -> "발주가 확정되었습니다.";
            case EventTypes.PURCHASE_ORDER_ACCEPTED -> "발주가 수락되었습니다.";
            case EventTypes.PURCHASE_ORDER_REJECTED -> "발주가 거절되었습니다.";
            case EventTypes.PURCHASE_ORDER_CANCELLED -> "발주가 취소되었습니다.";
            case EventTypes.SUB_PURCHASE_ORDER_CREATED -> "하위 발주가 생성되었습니다.";
            case EventTypes.SUB_PURCHASE_ORDER_CONFIRMED -> "하위 발주가 확정되었습니다.";
            case EventTypes.SUB_PURCHASE_ORDER_REJECTED -> "하위 발주가 거절되었습니다.";
            case EventTypes.SUB_PURCHASE_ORDER_CANCELLED -> "하위 발주가 취소되었습니다.";
            case EventTypes.SHIPMENT_CREATED -> "출하가 생성되었습니다.";
            case EventTypes.SHIPMENT_DEPARTED -> "출하가 출발했습니다.";
            case EventTypes.SHIPMENT_ARRIVED -> "출하가 도착했습니다.";
            case EventTypes.SHIPMENT_COMPLETED -> "출하가 완료되었습니다.";
            case EventTypes.SHIPMENT_DELAY_DETECTED -> "출하 지연이 감지되었습니다.";
            case EventTypes.DELIVERY_EXCEPTION_CREATED -> "배송 예외가 생성되었습니다.";
            case EventTypes.DELIVERY_EXCEPTION_DELAY -> "배송 지연 예외가 발생했습니다.";
            case EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION -> "온도 이탈 예외가 발생했습니다.";
            case EventTypes.DELIVERY_EXCEPTION_DAMAGED -> "파손 예외가 발생했습니다.";
            case EventTypes.LOGISTICS_NODE_CAPACITY_STATUS_CHANGED -> "물류 거점 용량 상태가 변경되었습니다.";
            case EventTypes.INVENTORY_SHORTAGE_DETECTED -> "재고 부족이 감지되었습니다.";
            case EventTypes.RETURN_REQUEST_CREATED -> "반품 요청이 생성되었습니다.";
            case EventTypes.RETURN_REQUEST_APPROVED -> "반품 요청이 승인되었습니다.";
            case EventTypes.RETURN_REQUEST_REJECTED -> "반품 요청이 거절되었습니다.";
            case EventTypes.RETURN_REQUEST_COMPLETED -> "반품 요청이 완료되었습니다.";
            case EventTypes.RETURN_REQUEST_CANCELLED -> "반품 요청이 취소되었습니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_CREATED -> "새 인증서가 등록되어 관리자 심사가 필요합니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_APPROVED -> "협력사 인증서가 승인되었습니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_REJECTED -> "협력사 인증서가 거절되었습니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRING -> "협력사 인증서 만료가 임박했습니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRED -> "협력사 인증서가 만료되었습니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_REVOKED -> "협력사 인증서가 철회되었습니다.";
            case EventTypes.SUPPLIER_SCORE_DROPPED -> "협력사 평가 점수가 하락했습니다.";
            case EventTypes.SUPPLIER_ESG_VIOLATED -> "협력사 ESG 기준 위반이 감지되었습니다.";
            default -> StringUtils.hasText(fallbackDescription)
                    ? fallbackDescription
                    : "공급망 이벤트가 발생했습니다.";
        };
    }

    private String resolveEventName(String eventType) {
        return switch (eventType) {
            case EventTypes.SHIPMENT_CREATED -> "출하 생성";
            case EventTypes.SHIPMENT_DEPARTED -> "출하 출발";
            case EventTypes.SHIPMENT_ARRIVED -> "출하 도착";
            case EventTypes.SHIPMENT_COMPLETED -> "출하 완료";
            case EventTypes.SHIPMENT_DELAY_DETECTED -> "출하 지연 감지";
            case EventTypes.DELIVERY_EXCEPTION_DELAY -> "배송 지연 예외";
            case EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION -> "온도 이탈 예외";
            case EventTypes.DELIVERY_EXCEPTION_DAMAGED -> "파손 예외";
            case EventTypes.LOGISTICS_NODE_CAPACITY_STATUS_CHANGED -> "물류 거점 용량 상태 변경";
            case EventTypes.PURCHASE_ORDER_ACCEPTED -> "발주 수락";
            case EventTypes.SUPPLIER_CERTIFICATE_CREATED -> "인증서 심사 요청";
            case EventTypes.SUPPLIER_CERTIFICATE_APPROVED -> "협력사 인증서 승인";
            case EventTypes.SUPPLIER_CERTIFICATE_REJECTED -> "협력사 인증서 반려";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRING -> "협력사 인증서 만료 임박";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRED -> "협력사 인증서 만료";
            default -> "공급망 이벤트";
        };
    }

    private String resolveEventDescription(String eventType) {
        return switch (eventType) {
            case EventTypes.SHIPMENT_CREATED -> "출하 생성 시";
            case EventTypes.SHIPMENT_DEPARTED -> "출하 출발 시";
            case EventTypes.SHIPMENT_ARRIVED -> "출하 도착 시";
            case EventTypes.SHIPMENT_COMPLETED -> "출하 완료 시";
            case EventTypes.SHIPMENT_DELAY_DETECTED -> "출하 지연 감지 시";
            case EventTypes.DELIVERY_EXCEPTION_DELAY -> "배송 지연 예외 발생 시";
            case EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION -> "온도 이탈 예외 발생 시";
            case EventTypes.DELIVERY_EXCEPTION_DAMAGED -> "파손 예외 발생 시";
            case EventTypes.LOGISTICS_NODE_CAPACITY_STATUS_CHANGED -> "물류 거점 용량 상태 변경 시";
            case EventTypes.PURCHASE_ORDER_ACCEPTED -> "발주 수락 시";
            case EventTypes.SUPPLIER_CERTIFICATE_CREATED -> "새 인증서가 등록되어 관리자 심사가 필요합니다.";
            case EventTypes.SUPPLIER_CERTIFICATE_APPROVED -> "협력사 인증서 승인 시";
            case EventTypes.SUPPLIER_CERTIFICATE_REJECTED -> "협력사 인증서 반려 시";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRING -> "협력사 인증서 만료 임박 시";
            case EventTypes.SUPPLIER_CERTIFICATE_EXPIRED -> "협력사 인증서 만료 시";
            default -> "공급망 이벤트 발생 시";
        };
    }

    private DomainType resolveDomainType(String eventType) {
        if (eventType.startsWith("purchase-order.") || eventType.startsWith("sub-purchase-order.")) {
            return DomainType.ORDER;
        }
        if (eventType.startsWith("shipment.") || eventType.startsWith("delivery-exception.")) {
            return DomainType.SHIPMENT;
        }
        if (eventType.startsWith("return-request.")) {
            return DomainType.RETURN_REQUEST;
        }
        if (eventType.startsWith("supplier-certificate.") || eventType.startsWith("supplier.")) {
            return DomainType.SUPPLIER;
        }
        return DomainType.SYSTEM;
    }

    private NotificationToastType resolveToastType(String eventType) {
        return switch (eventType) {
            case EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION,
                 EventTypes.DELIVERY_EXCEPTION_DAMAGED,
                 EventTypes.SUPPLIER_CERTIFICATE_EXPIRED,
                 EventTypes.SUPPLIER_ESG_VIOLATED ->
                    NotificationToastType.RISK_ALERT;
            case EventTypes.SHIPMENT_DELAY_DETECTED,
                 EventTypes.DELIVERY_EXCEPTION_DELAY,
                 EventTypes.INVENTORY_SHORTAGE_DETECTED,
                 EventTypes.SUPPLIER_CERTIFICATE_REJECTED,
                 EventTypes.SUPPLIER_CERTIFICATE_EXPIRING,
                 EventTypes.PURCHASE_ORDER_REJECTED,
                 EventTypes.PURCHASE_ORDER_CANCELLED,
                 EventTypes.SUB_PURCHASE_ORDER_REJECTED,
                 EventTypes.SUB_PURCHASE_ORDER_CANCELLED,
                 EventTypes.RETURN_REQUEST_REJECTED,
                 EventTypes.RECOMMENDATION_REQUESTED ->
                    NotificationToastType.WARNING;
            case EventTypes.PURCHASE_ORDER_CONFIRMED,
                 EventTypes.PURCHASE_ORDER_ACCEPTED,
                 EventTypes.SUB_PURCHASE_ORDER_CONFIRMED,
                 EventTypes.SHIPMENT_COMPLETED,
                 EventTypes.RETURN_REQUEST_APPROVED,
                 EventTypes.RETURN_REQUEST_COMPLETED,
                 EventTypes.SUPPLIER_CERTIFICATE_APPROVED,
                 EventTypes.RECOMMENDATION_GENERATED,
                 EventTypes.RECOMMENDATION_ACCEPTED ->
                    NotificationToastType.SUCCESS;
            default -> NotificationToastType.INFO;
        };
    }

    private String resolveDeepLink(String eventType, String referencePublicId) {
        if (!StringUtils.hasText(referencePublicId)) {
            return null;
        }
        if (eventType.startsWith("purchase-order.")) {
            return "/operations/orders/" + referencePublicId;
        }
        if (eventType.startsWith("sub-purchase-order.")) {
            return "/operations/sub-orders/" + referencePublicId;
        }
        if (eventType.startsWith("shipment.") || eventType.startsWith("delivery-exception.")) {
            return "/operations/shipments/" + referencePublicId;
        }
        if (eventType.startsWith("return-request.")) {
            return "/operations/returns/" + referencePublicId;
        }
        if (EventTypes.SUPPLIER_CERTIFICATE_CREATED.equals(eventType)) {
            return "/certificate-review";
        }
        if (eventType.startsWith("supplier-certificate.") || eventType.startsWith("supplier.")) {
            return "/suppliers";
        }
        return null;
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asText();
    }
}
