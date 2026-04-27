package com.ozz.atlas.control.kafka.notification;

import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import com.ozz.atlas.control.notification.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class SupplyChainNotificationHandler extends AbstractRuleBasedKafkaNotificationHandler {

    public SupplyChainNotificationHandler(
            KafkaEventRuleService kafkaEventRuleService,
            NotificationRecipientResolver notificationRecipientResolver,
            NotificationCommandFactory notificationCommandFactory,
            NotificationService notificationService
    ) {
        super(kafkaEventRuleService, notificationRecipientResolver, notificationCommandFactory, notificationService);
    }

    @Override
    public boolean supports(String eventType) {
        return eventType.startsWith("purchase-order.")
                || eventType.startsWith("sub-purchase-order.")
                || eventType.startsWith("shipment.")
                || eventType.startsWith("delivery-exception.")
                || eventType.startsWith("logistics-node.")
                || eventType.startsWith("inventory.")
                || eventType.startsWith("lot.")
                || eventType.startsWith("return-request.")
                || eventType.startsWith("supplier-certificate.")
                || eventType.startsWith("supplier.");
    }
}
