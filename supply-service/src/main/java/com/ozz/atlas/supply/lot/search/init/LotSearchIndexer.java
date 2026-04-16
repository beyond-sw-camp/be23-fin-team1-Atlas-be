package com.ozz.atlas.supply.lot.search.init;

import com.ozz.atlas.supply.lot.search.repository.LotSearchRepository;
import com.ozz.atlas.supply.lot.search.service.LotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotSearchIndexer implements ApplicationRunner {

    // lots 인덱스 상태를 확인하려고 repository를 같이 주입
    private final LotSearchRepository lotSearchRepository;

    // 인덱스가 비어 있으면 전체 LOT 데이터를 다시 적재
    private final LotSearchService lotSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 기존 ES 패턴과 동일하게, 인덱스가 비어 있을 때만 전체 재색인
        if (lotSearchRepository.count() == 0) {
            lotSearchService.reindexAllLots();
        }
    }
}
