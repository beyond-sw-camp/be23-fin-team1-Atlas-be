package com.ozz.atlas.control.kafka.log.search.repository;

import com.ozz.atlas.control.kafka.log.search.document.EventLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogSearchRepository extends ElasticsearchRepository<EventLogDocument, Long> {
}
