package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.control.client.AuthServiceClient;
import com.ozz.atlas.control.client.dto.AuthUserRecipientDto;
import com.ozz.atlas.control.kafka.recommendation.RecommendationDecisionPayload;
import com.ozz.atlas.control.notification.service.NotificationPreferenceService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationRecipientResolver {

    private static final String PURCHASE_DEPARTMENT = "PURCHASE_DEPARTMENT";
    private static final String LOGISTICS_DEPARTMENT = "LOGISTICS_DEPARTMENT";
    private static final String QUALITY_DEPARTMENT = "QUALITY_DEPARTMENT";

    private final ObjectMapper objectMapper;
    private final AuthServiceClient authServiceClient;
    private final NotificationPreferenceService notificationPreferenceService;

    public List<String> resolve(EventEnvelope<JsonNode> eventEnvelope) {
        if (isSupplyEvent(eventEnvelope.eventType())) {
            return resolveSupplyRecipients(eventEnvelope);
        }

        Set<String> recipientUserPublicIds = new LinkedHashSet<>();

        addIfHasText(recipientUserPublicIds, eventEnvelope.actorUserPublicId());

        if (EventTypes.RECOMMENDATION_ACCEPTED.equals(eventEnvelope.eventType())
                || EventTypes.RECOMMENDATION_REJECTED.equals(eventEnvelope.eventType())) {
            RecommendationDecisionPayload payload =
                    objectMapper.convertValue(eventEnvelope.payload(), RecommendationDecisionPayload.class);
            addIfHasText(recipientUserPublicIds, payload.decidedByUserPublicId());
        }

        return notificationPreferenceService.filterEnabledRecipients(
                eventEnvelope.eventType(),
                List.copyOf(recipientUserPublicIds)
        );
    }

    private void addIfHasText(Set<String> recipientUserPublicIds, String userPublicId) {
        if (StringUtils.hasText(userPublicId)) {
            recipientUserPublicIds.add(userPublicId);
        }
    }

    private List<String> resolveSupplyRecipients(EventEnvelope<JsonNode> eventEnvelope) {
        Set<String> recipientUserPublicIds = new LinkedHashSet<>();
        String departmentCode = resolveDepartmentCode(eventEnvelope);
        Set<String> organizationPublicIds = resolveOrganizationPublicIds(eventEnvelope);
        String actorUserPublicId = resolveActorUserPublicId(eventEnvelope);

        for (String organizationPublicId : organizationPublicIds) {
            authServiceClient.getNotificationRecipients(organizationPublicId, departmentCode).stream()
                    .map(AuthUserRecipientDto::userPublicId)
                    .filter(StringUtils::hasText)
                    .filter(userPublicId -> !userPublicId.equals(actorUserPublicId))
                    .forEach(recipientUserPublicIds::add);
        }

        return notificationPreferenceService.filterEnabledRecipients(
                eventEnvelope.eventType(),
                List.copyOf(recipientUserPublicIds)
        );
    }

    private Set<String> resolveOrganizationPublicIds(EventEnvelope<JsonNode> eventEnvelope) {
        Set<String> organizationPublicIds = new LinkedHashSet<>();
        JsonNode payload = eventEnvelope.payload();

        addIfHasText(organizationPublicIds, text(payload, "rootBuyerOrganizationPublicId"));
        addIfHasText(organizationPublicIds, text(payload, "directBuyerOrganizationPublicId"));
        addIfHasText(organizationPublicIds, text(payload, "directSupplierOrganizationPublicId"));
        addIfHasText(organizationPublicIds, eventEnvelope.organizationPublicId());

        return organizationPublicIds;
    }

    private String resolveDepartmentCode(EventEnvelope<JsonNode> eventEnvelope) {
        String eventType = eventEnvelope.eventType();
        if (eventType.startsWith("purchase-order.") || eventType.startsWith("sub-purchase-order.")) {
            return PURCHASE_DEPARTMENT;
        }
        if (eventType.startsWith("shipment.")
                || eventType.startsWith("delivery-exception.")
                || eventType.startsWith("logistics-node.")
                || eventType.startsWith("inventory.")) {
            return LOGISTICS_DEPARTMENT;
        }
        if (eventType.startsWith("supplier-certificate.")
                || eventType.startsWith("supplier.")) {
            return QUALITY_DEPARTMENT;
        }
        if (eventType.startsWith("return-request.")) {
            String returnType = text(eventEnvelope.payload(), "returnType");
            if ("DEFECTIVE".equals(returnType)) {
                return QUALITY_DEPARTMENT;
            }
            return LOGISTICS_DEPARTMENT;
        }
        return null;
    }

    private String resolveActorUserPublicId(EventEnvelope<JsonNode> eventEnvelope) {
        if (StringUtils.hasText(eventEnvelope.actorUserPublicId())) {
            return eventEnvelope.actorUserPublicId();
        }
        return text(eventEnvelope.payload(), "actorUserPublicId");
    }

    private boolean isSupplyEvent(String eventType) {
        return eventType.startsWith("purchase-order.")
                || eventType.startsWith("sub-purchase-order.")
                || eventType.startsWith("shipment.")
                || eventType.startsWith("delivery-exception.")
                || eventType.startsWith("logistics-node.")
                || eventType.startsWith("inventory.")
                || eventType.startsWith("return-request.")
                || eventType.startsWith("supplier-certificate.")
                || eventType.startsWith("supplier.");
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asText();
    }
}
