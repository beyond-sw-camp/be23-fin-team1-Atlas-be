package com.ozz.atlas.control.kafka.log.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.control.kafka.log.EventLog;
import com.ozz.atlas.control.kafka.log.EventLogRepository;
import com.ozz.atlas.control.kafka.log.search.document.EventLogDocument;
import com.ozz.atlas.control.kafka.log.search.dtos.EventLogSearchDto;
import com.ozz.atlas.control.kafka.log.search.dtos.EventLogSearchResponse;
import com.ozz.atlas.control.kafka.log.search.repository.EventLogSearchRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLogSearchService {

    private final EventLogSearchRepository eventLogSearchRepository;
    private final EventLogRepository eventLogRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // DB에 저장된 EventLog를 Elasticsearch 문서로 저장
    @Transactional
    public void saveEventLogDocument(EventLog eventLog) {
        eventLogSearchRepository.save(EventLogDocument.fromEntity(eventLog));
    }

    // Kafka 감사로그를 Elasticsearch에서 검색
    public Page<EventLogSearchResponse> search(Pageable pageable, EventLogSearchDto searchDto) {
        if (searchDto == null) {
            searchDto = new EventLogSearchDto();
        }

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 검색어가 있으면 여러 필드를 한 번에 부분 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        // 토픽명으로 정확히 필터링
        if (hasText(searchDto.getTopic())) {
            filterQueries.add(termQuery("topic", searchDto.getTopic()));
        }

        // 이벤트 타입으로 정확히 필터링
        if (hasText(searchDto.getEventType())) {
            filterQueries.add(termQuery("eventType", searchDto.getEventType()));
        }

        // 이벤트 대상 도메인 종류로 필터링
        if (searchDto.getAggregateType() != null) {
            filterQueries.add(termQuery("aggregateType", searchDto.getAggregateType().name()));
        }

        // 이벤트 대상 publicId로 정확히 필터링
        if (hasText(searchDto.getAggregatePublicId())) {
            filterQueries.add(termQuery("aggregatePublicId", searchDto.getAggregatePublicId()));
        }

        // Kafka 발행 결과 상태로 필터링
        if (searchDto.getStatus() != null) {
            filterQueries.add(termQuery("status", searchDto.getStatus().name()));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<EventLogDocument> searchHits =
                elasticsearchOperations.search(query, EventLogDocument.class);

        List<EventLogSearchResponse> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB에 있는 Kafka 감사로그 전체를 Elasticsearch로 다시 색인
    @Transactional
    public void reindexAllEventLogs() {
        eventLogRepository.findAll().forEach(this::saveEventLogDocument);
    }

    // 통합 키워드 검색용
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("topic.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("eventType.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("aggregatePublicId.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("eventJson.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("lastError.ngram")
                        .query(keyword)
                ))
                .minimumShouldMatch("1")
        ));
    }

    private Query buildFinalQuery(List<Query> mustQueries, List<Query> filterQueries) {
        if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        return Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }

            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }

            return b;
        }));
    }

    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    private EventLogSearchResponse toResponse(EventLogDocument document) {
        return EventLogSearchResponse.builder()
                .id(document.getEventLogId())
                .eventId(document.getEventId())
                .topic(document.getTopic())
                .eventType(document.getEventType())
                .aggregateType(document.getAggregateType())
                .aggregatePublicId(document.getAggregatePublicId())
                .eventJson(document.getEventJson())
                .status(document.getStatus())
                .publishedAt(document.getPublishedAt())
                .lastError(document.getLastError())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    // null, 빈 문자열, 공백 문자열을 한 번에 걸러냄
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
