package com.ozz.atlas.supply.shipment.search.repository;

import com.ozz.atlas.supply.shipment.search.document.ShipmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentSearchRepository extends ElasticsearchRepository<ShipmentDocument, Long> {
}
