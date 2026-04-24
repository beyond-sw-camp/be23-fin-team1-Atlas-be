package com.ozz.atlas.control.kafka.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.ozz.atlas.common.kafka.EventEnvelope;
import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import com.ozz.atlas.control.notification.command.NotificationCommand;
import com.ozz.atlas.control.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRuleBasedKafkaNotificationHandler implements KafkaNotificationHandler {

    private final KafkaEventRuleService kafkaEventRuleService;
    private final NotificationRecipientResolver notificationRecipientResolver;
    private final NotificationCommandFactory notificationCommandFactory;
    private final NotificationService notificationService;

    @Override
    public void handle(EventEnvelope<JsonNode> eventEnvelope) {
        if (!kafkaEventRuleService.isEnabled(eventEnvelope.eventType())) {
            log.info("비활성화된 Kafka 이벤트 규칙입니다. eventType={}", eventEnvelope.eventType());
            return;
        }

        List<String> recipientUserPublicIds = notificationRecipientResolver.resolve(eventEnvelope);
        if (recipientUserPublicIds.isEmpty()) {
            log.info("알림 수신 대상이 없어 알림 생성을 건너뜁니다. eventType={}, eventId={}",
                    eventEnvelope.eventType(), eventEnvelope.eventId());
            return;
        }

        List<NotificationCommand> commands =
                notificationCommandFactory.create(eventEnvelope, recipientUserPublicIds);
        if (commands.isEmpty()) {
            log.info("생성할 알림 command가 없습니다. eventType={}, eventId={}",
                    eventEnvelope.eventType(), eventEnvelope.eventId());
            return;
        }

        commands.forEach(notificationService::saveAndPublish);
        kafkaEventRuleService.markTriggered(eventEnvelope.eventType());
    }
}
