package com.ozz.atlas.supply.settlement.search.repository;

import com.ozz.atlas.supply.settlement.search.document.SettlementDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementSearchRepository extends ElasticsearchRepository<SettlementDocument, Long> {
}
