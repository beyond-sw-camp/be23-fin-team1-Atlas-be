package com.ozz.atlas.control.kafka.processed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }

    @Transactional
    public void markProcessed(String eventId, String eventType, String topic, String aggregatePublicId) {
        if (processedEventRepository.existsByEventId(eventId)) {
            return;
        }
        processedEventRepository.save(ProcessedEvent.of(eventId, eventType, topic, aggregatePublicId));
    }
}
