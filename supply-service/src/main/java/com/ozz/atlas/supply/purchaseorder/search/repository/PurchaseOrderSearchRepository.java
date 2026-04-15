package com.ozz.atlas.supply.purchaseorder.search.repository;

import com.ozz.atlas.supply.purchaseorder.search.document.PurchaseOrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderSearchRepository extends ElasticsearchRepository<PurchaseOrderDocument, Long> {
}
