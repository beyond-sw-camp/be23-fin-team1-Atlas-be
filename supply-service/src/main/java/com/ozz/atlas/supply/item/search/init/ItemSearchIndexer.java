package com.ozz.atlas.supply.item.search.init;

import com.ozz.atlas.supply.item.search.repository.ItemSearchRepository;
import com.ozz.atlas.supply.item.search.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemSearchIndexer implements ApplicationRunner {

    private final ItemSearchRepository itemSearchRepository;
    private final ItemSearchService itemSearchService;

    @Override
    public void run(ApplicationArguments args) {
        // 기존 ES 패턴과 동일하게, 인덱스가 비어 있을 때만 전체 재색인
        if (itemSearchRepository.count() == 0) {
            itemSearchService.reindexAllItems();
        }
    }
}
