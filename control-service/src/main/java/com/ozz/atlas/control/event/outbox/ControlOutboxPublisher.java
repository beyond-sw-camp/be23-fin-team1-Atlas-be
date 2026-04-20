package com.ozz.atlas.control.event.outbox;

import com.ozz.atlas.control.event.log.EventLog;
import com.ozz.atlas.control.event.log.EventLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ControlOutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final long RETRY_DELAY_SECONDS = 30L;

    private final OutboxEventRepository outboxEventRepository;
    private final EventLogRepository eventLogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${atlas.kafka.outbox.publish-delay-ms:2000}")
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findPublishableEvents(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );

        for (OutboxEvent outboxEvent : events) {
            publishSingleEvent(outboxEvent);
        }
    }

    @Transactional
    protected void publishSingleEvent(OutboxEvent outboxEvent) {
        try {
            kafkaTemplate.send(
                    outboxEvent.getTopic(),
                    outboxEvent.getPartitionKey(),
                    outboxEvent.getEventJson()
            ).get();

            upsertPublishedLog(outboxEvent);
            outboxEventRepository.delete(outboxEvent);
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getRootCauseMessage(e);
            outboxEvent.markFailed(errorMessage, LocalDateTime.now().plusSeconds(RETRY_DELAY_SECONDS));
            outboxEventRepository.save(outboxEvent);
            upsertFailedLog(outboxEvent, errorMessage);
        }
    }

    private void upsertPublishedLog(OutboxEvent outboxEvent) {
        eventLogRepository.findByEventId(outboxEvent.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            existing.markPublished(outboxEvent);
                            eventLogRepository.save(existing);
                        },
                        () -> eventLogRepository.save(EventLog.publishedFrom(outboxEvent))
                );
    }

    private void upsertFailedLog(OutboxEvent outboxEvent, String errorMessage) {
        eventLogRepository.findByEventId(outboxEvent.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            existing.markFailed(outboxEvent, errorMessage);
                            eventLogRepository.save(existing);
                        },
                        () -> eventLogRepository.save(EventLog.failedFrom(outboxEvent, errorMessage))
                );
    }
}
