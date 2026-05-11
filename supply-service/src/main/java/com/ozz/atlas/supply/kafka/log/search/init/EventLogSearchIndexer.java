package com.ozz.atlas.supply.kafka.log.search.init;

import com.ozz.atlas.supply.kafka.log.search.service.EventLogSearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventLogSearchIndexer implements CommandLineRunner {

    private final EventLogSearchIndexService eventLogSearchIndexService;

    @Override
    public void run(String... args) {
        eventLogSearchIndexService.reindexAllEventLogs();
    }
}
