package com.ozz.atlas.supply.returns.search.init;

import com.ozz.atlas.supply.returns.search.repository.ReturnSearchRepository;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnSearchIndexer implements ApplicationRunner {

    // returns 인덱스 상태를 확인하려고 repository를 같이 주입
    private final ReturnSearchRepository returnSearchRepository;

    // 인덱스가 비어 있으면 전체 반품 데이터를 다시 적재
    private final ReturnSearchService returnSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 기존 ES 패턴과 동일하게, 인덱스가 비어 있을 때만 전체 재색인
        if (returnSearchRepository.count() == 0) {
            returnSearchService.reindexAllReturns();
        }
    }
}
