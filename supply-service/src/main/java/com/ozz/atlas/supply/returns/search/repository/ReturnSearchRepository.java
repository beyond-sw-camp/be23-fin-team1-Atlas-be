package com.ozz.atlas.supply.returns.search.repository;

import com.ozz.atlas.supply.returns.search.document.ReturnDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnSearchRepository extends ElasticsearchRepository<ReturnDocument, Long> {
}
