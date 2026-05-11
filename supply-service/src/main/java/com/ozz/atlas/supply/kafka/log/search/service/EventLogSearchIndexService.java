package com.ozz.atlas.supply.kafka.log.search.service;

import com.ozz.atlas.supply.kafka.log.EventLog;
import com.ozz.atlas.supply.kafka.log.EventLogRepository;
import com.ozz.atlas.supply.kafka.log.search.document.EventLogDocument;
import com.ozz.atlas.supply.kafka.log.search.repository.EventLogSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLogSearchIndexService {

    private final EventLogSearchRepository eventLogSearchRepository;
    private final EventLogRepository eventLogRepository;

    @Transactional
    public void saveEventLogDocument(EventLog eventLog) {
        eventLogSearchRepository.save(EventLogDocument.fromEntity(eventLog));
    }

    @Transactional
    public void reindexAllEventLogs() {
        eventLogRepository.findAll().forEach(this::saveEventLogDocument);
    }
}
