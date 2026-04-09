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
        if (organizationSearchRepository.count() == 0) {
            organizationSearchService.reindexAllOrganizations();
        }
    }
}
