package com.ozz.atlas.supply.item.search.repository;

import com.ozz.atlas.supply.item.search.document.ItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSearchRepository extends ElasticsearchRepository<ItemDocument, Long> {
}
