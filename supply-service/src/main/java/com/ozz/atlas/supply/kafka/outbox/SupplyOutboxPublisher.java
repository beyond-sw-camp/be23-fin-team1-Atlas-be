package com.ozz.atlas.supply.kafka.outbox;

import com.ozz.atlas.supply.kafka.log.EventLog;
import com.ozz.atlas.supply.kafka.log.EventLogRepository;
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
public class SupplyOutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final long RETRY_DELAY_SECONDS = 30L;

    private final OutboxEventRepository outboxEventRepository;
    private final EventLogRepository eventLogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${atlas.kafka.outbox.publish-delay-ms:2000}")
    public void publishPendingEvents() {
        // 성공하지 못한 이벤트까지 함께 읽어서 재시도
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
            // 실제 Kafka 발행은 outbox publisher가 담당, 서비스 계층은 DB
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
