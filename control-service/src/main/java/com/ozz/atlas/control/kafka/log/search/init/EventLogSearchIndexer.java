package com.ozz.atlas.control.kafka.log.search.init;

import com.ozz.atlas.control.kafka.log.search.service.EventLogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 서버가 시작될 때 DB에 이미 저장된 Kafka 감사 로그를 Elasticsearch에 다시 적재
 *
 * 기존 ES 구조와 맞추기 위한 초기 색인 클래스
 * 예를 들어 event_log 테이블에는 데이터가 있는데 event-logs 인덱스가 비어 있으면
 * 프론트 검색 결과가 0건
 */
@Component
@RequiredArgsConstructor
public class EventLogSearchIndexer implements CommandLineRunner {

    /**
     * Kafka 감사 로그 ES 저장/검색을 담당하는 서비스
     */
    private final EventLogSearchService eventLogSearchService;

    /**
     * 애플리케이션 시작 직후 한 번 실행
     *
     * DB event_log 전체 데이터를 읽어서
     * Elasticsearch event-logs 인덱스에 다시 저장
     */
    @Override
    public void run(String... args) {
        eventLogSearchService.reindexAllEventLogs();
    }
}
