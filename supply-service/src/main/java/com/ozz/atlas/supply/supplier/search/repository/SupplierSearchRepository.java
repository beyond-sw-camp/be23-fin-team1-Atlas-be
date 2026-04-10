package com.ozz.atlas.supply.supplier.search.repository;

import com.ozz.atlas.supply.supplier.search.document.SupplierDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierSearchRepository extends ElasticsearchRepository<SupplierDocument, Long> {
}
