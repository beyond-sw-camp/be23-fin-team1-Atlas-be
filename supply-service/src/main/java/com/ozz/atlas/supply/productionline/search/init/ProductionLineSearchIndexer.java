package com.ozz.atlas.supply.productionline.search.init;

import com.ozz.atlas.supply.productionline.search.repository.ProductionLineSearchRepository;
import com.ozz.atlas.supply.productionline.search.service.ProductionLineSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductionLineSearchIndexer implements ApplicationRunner {

    // production-lines 인덱스 상태를 확인하려고 repository를 같이 주입
    private final ProductionLineSearchRepository productionLineSearchRepository;

    // 인덱스가 비어 있으면 전체 생산라인 데이터를 다시 적재
    private final ProductionLineSearchService productionLineSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 기존 ES 패턴과 동일하게, 인덱스가 비어 있을 때만 전체 재색인
        if (productionLineSearchRepository.count() == 0) {
            productionLineSearchService.reindexAllProductionLines();
        }
    }
}
