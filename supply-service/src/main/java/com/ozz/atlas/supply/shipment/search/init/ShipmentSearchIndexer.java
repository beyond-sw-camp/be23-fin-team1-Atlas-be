package com.ozz.atlas.supply.shipment.search.init;

import com.ozz.atlas.supply.shipment.search.repository.ShipmentSearchRepository;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentSearchIndexer implements ApplicationRunner {

    private final ShipmentSearchRepository shipmentSearchRepository;
    private final ShipmentSearchService shipmentSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 인덱스가 비어 있으면 기존 DB 데이터를 한 번 초기 적재
        if (shipmentSearchRepository.count() == 0) {
            shipmentSearchService.reindexAllShipments();
        }
    }
}
