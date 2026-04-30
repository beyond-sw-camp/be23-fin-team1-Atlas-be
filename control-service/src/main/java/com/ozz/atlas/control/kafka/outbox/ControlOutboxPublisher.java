package com.ozz.atlas.control.kafka.outbox;

import com.ozz.atlas.control.kafka.log.EventLog;
import com.ozz.atlas.control.kafka.log.EventLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ozz.atlas.control.kafka.log.search.service.EventLogSearchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControlOutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final long RETRY_DELAY_SECONDS = 30L;

    private final OutboxEventRepository outboxEventRepository;
    private final EventLogRepository eventLogRepository;
    private final EventLogSearchService eventLogSearchService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${atlas.kafka.outbox.publish-delay-ms:2000}")
    public void publishPendingEvents() {
        // 성공하지 못한 FAILED 이벤트까지 함께 읽어 재시도한다.
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
            // 실제 Kafka 발행 책임은 publisher가 가진다. 서비스 계층은 outbox 적재까지만 담당한다.
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
                            // 기존 로그가 있으면 발행 성공 상태로 갱신
                            existing.markPublished(outboxEvent);

                            // DB에 먼저 저장해야 id, updatedAt 같은 값이 확정
                            EventLog savedLog = eventLogRepository.save(existing);

                            // DB 저장이 끝난 최신 상태를 Elasticsearch에도 반영
                            saveEventLogDocumentSafely(savedLog);

                        },
                        () -> {
                            // 처음 발행되는 이벤트면 새 감사로그를 만듬
                            EventLog newLog = EventLog.publishedFrom(outboxEvent);

                            // 새 감사로그도 DB 저장 후 Elasticsearch에 저장
                            EventLog savedLog = eventLogRepository.save(newLog);
                            saveEventLogDocumentSafely(savedLog);

                        }
                );
    }

    private void upsertFailedLog(OutboxEvent outboxEvent, String errorMessage) {
        eventLogRepository.findByEventId(outboxEvent.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            // 기존 로그가 있으면 발행 실패 상태와 마지막 에러를 갱신
                            existing.markFailed(outboxEvent, errorMessage);

                            // DB 저장 후 확정된 값을 Elasticsearch에도 반영
                            EventLog savedLog = eventLogRepository.save(existing);
                            saveEventLogDocumentSafely(savedLog);

                        },
                        () -> {
                            // 처음 실패한 이벤트면 실패 상태 감사로그를 새로 만듬
                            EventLog newLog = EventLog.failedFrom(outboxEvent, errorMessage);

                            // 새 실패 로그도 DB 저장 후 Elasticsearch에 저장
                            EventLog savedLog = eventLogRepository.save(newLog);
                            saveEventLogDocumentSafely(savedLog);

                        }
                );
    }

    private void saveEventLogDocumentSafely(EventLog eventLog) {
        try {
            eventLogSearchService.saveEventLogDocument(eventLog);
        } catch (Exception e) {
            log.warn("Kafka 감사로그 Elasticsearch 저장에 실패했습니다. eventId={}", eventLog.getEventId(), e);
        }
    }


}
