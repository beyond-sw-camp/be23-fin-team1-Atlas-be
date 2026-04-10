package com.ozz.atlas.supply.supplier.search.init;

import com.ozz.atlas.supply.supplier.search.repository.SupplierSearchRepository;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplierSearchIndexer implements ApplicationRunner {

    private final SupplierSearchRepository supplierSearchRepository;
    private final SupplierSearchService supplierSearchService;

    // ES 인덱스가 비어 있으면 전체 협력사 데이터를 초기 색인
    @Override
    public void run(ApplicationArguments args) {
        if (supplierSearchRepository.count() == 0) {
            supplierSearchService.reindexAllSuppliers();
        }
    }
}
