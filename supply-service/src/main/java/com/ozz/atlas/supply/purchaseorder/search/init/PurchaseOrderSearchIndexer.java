package com.ozz.atlas.supply.purchaseorder.search.init;

import com.ozz.atlas.supply.purchaseorder.search.repository.PurchaseOrderSearchRepository;
import com.ozz.atlas.supply.purchaseorder.search.service.PurchaseOrderSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseOrderSearchIndexer implements ApplicationRunner {

    // purchase-order 인덱스 상태를 확인하려고 repository도 같이 주입
    private final PurchaseOrderSearchRepository purchaseOrderSearchRepository;

    // 인덱스가 비었을 때 전체 재색인을 실행하는 service
    private final PurchaseOrderSearchService purchaseOrderSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 인덱스가 비어 있을 때만 전체 발주를 다시 적재
        if (purchaseOrderSearchRepository.count() == 0) {
            purchaseOrderSearchService.reindexAllPurchaseOrders();
        }
    }
}
