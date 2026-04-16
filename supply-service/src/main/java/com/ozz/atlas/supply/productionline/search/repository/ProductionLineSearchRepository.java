package com.ozz.atlas.supply.productionline.search.repository;

import com.ozz.atlas.supply.productionline.search.document.ProductionLineDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionLineSearchRepository extends ElasticsearchRepository<ProductionLineDocument, Long> {
}
