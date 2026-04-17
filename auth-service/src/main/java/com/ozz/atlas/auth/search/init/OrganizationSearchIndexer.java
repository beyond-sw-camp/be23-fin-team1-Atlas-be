package com.ozz.atlas.auth.search.init;

import com.ozz.atlas.auth.search.repository.OrganizationSearchRepository;
import com.ozz.atlas.auth.search.service.OrganizationSearchService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OrganizationSearchIndexer implements ApplicationRunner {

    private final OrganizationSearchRepository organizationSearchRepository;
    private final OrganizationSearchService organizationSearchService;

    public OrganizationSearchIndexer(OrganizationSearchRepository organizationSearchRepository,
                                     OrganizationSearchService organizationSearchService) {
        this.organizationSearchRepository = organizationSearchRepository;
        this.organizationSearchService = organizationSearchService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 로컬에서는 DB 기준으로 검색 인덱스를 매번 다시 맞춤
        // DB에 직접 넣은 조직 데이터도 검색에 잡히게 하려는 목적
        organizationSearchService.reindexAllOrganizations();
    }
}
