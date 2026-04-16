package com.ozz.atlas.supply.settlement.search.init;

import com.ozz.atlas.supply.settlement.search.repository.SettlementSearchRepository;
import com.ozz.atlas.supply.settlement.search.service.SettlementSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementSearchIndexer implements ApplicationRunner {

    // settlements 인덱스 상태를 확인하려고 repository를 같이 주입
    private final SettlementSearchRepository settlementSearchRepository;

    // 인덱스가 비어 있으면 전체 정산 데이터를 다시 적재
    private final SettlementSearchService settlementSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 기존 ES 패턴과 동일하게, 인덱스가 비어 있을 때만 전체 재색인
        if (settlementSearchRepository.count() == 0) {
            settlementSearchService.reindexAllSettlements();
        }
    }
}
