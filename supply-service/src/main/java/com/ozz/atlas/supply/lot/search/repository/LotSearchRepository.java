package com.ozz.atlas.supply.lot.search.repository;

import com.ozz.atlas.supply.lot.search.document.LotDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotSearchRepository extends ElasticsearchRepository<LotDocument, Long> {
}
