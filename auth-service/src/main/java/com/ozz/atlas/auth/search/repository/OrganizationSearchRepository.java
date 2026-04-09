    package com.ozz.atlas.auth.search.repository;

    import com.ozz.atlas.auth.search.document.OrganizationDocument;
    import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface OrganizationSearchRepository extends ElasticsearchRepository<OrganizationDocument, Long> {
    }
