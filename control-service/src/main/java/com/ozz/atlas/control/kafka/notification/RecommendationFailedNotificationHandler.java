package com.ozz.atlas.control.kafka.notification;

import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import com.ozz.atlas.control.notification.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class RecommendationFailedNotificationHandler extends AbstractRuleBasedKafkaNotificationHandler {

    public RecommendationFailedNotificationHandler(
            KafkaEventRuleService kafkaEventRuleService,
            NotificationRecipientResolver notificationRecipientResolver,
            NotificationCommandFactory notificationCommandFactory,
            NotificationService notificationService
    ) {
        super(kafkaEventRuleService, notificationRecipientResolver, notificationCommandFactory, notificationService);
    }

    @Override
    public boolean supports(String eventType) {
        return EventTypes.RECOMMENDATION_FAILED.equals(eventType);
    }
}
