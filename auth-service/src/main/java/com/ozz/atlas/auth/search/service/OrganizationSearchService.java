package com.ozz.atlas.auth.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.dtos.OrganizationListDto;
import com.ozz.atlas.auth.dtos.OrganizationSearchDto;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.search.document.OrganizationDocument;
import com.ozz.atlas.auth.search.repository.OrganizationSearchRepository;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationSearchService {

    private final OrganizationSearchRepository organizationSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationSearchService(OrganizationSearchRepository organizationSearchRepository,
                                     ElasticsearchOperations elasticsearchOperations,
                                     OrganizationRepository organizationRepository) {
        this.organizationSearchRepository = organizationSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.organizationRepository = organizationRepository;
    }

    // 조직 정보 ES 문서로 저장
    public void saveOrganizationDocument(Organization organization) {
        organizationSearchRepository.save(OrganizationDocument.fromEntity(organization));
    }

    // 조직 문서를 ES에서 삭제
    public void deleteOrganizationDocument(Long organizationId) {
        organizationSearchRepository.deleteById(organizationId);
    }

    // 조직 검색
    public Page<OrganizationListDto> search(Pageable pageable, OrganizationSearchDto searchDto) {
        List<Query> filters = new ArrayList<>();

        // 조직 타입 조건 추가
        if (searchDto.getOrganizationType() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("organizationType.keyword")
                    .value(searchDto.getOrganizationType().name()))));
        }

        // 상태 조건 추가
        Status status = searchDto.getStatus() != null ? searchDto.getStatus() : Status.ACTIVE;
        filters.add(Query.of(q -> q.term(t -> t.field("status.keyword")
                .value(status.name()))));

        // keyword 검색 조건
        Query keywordQuery = Query.of(q -> q.multiMatch(m -> m
                .query(searchDto.getKeyword())
                .fields(List.of(
                        "organizationName",
                        "businessNo",
                        "contactFirstName",
                        "contactMiddleName",
                        "contactLastName",
                        "contactEmail",
                        "contactPhone"
                ))
        ));

        // 최종 ES 쿼리
        Query finalQuery = Query.of(q -> q.bool(b -> b
                .must(keywordQuery)
                .filter(filters)
        ));

        // 페이지 정보 포함
        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .build();

        // ES 검색 실행
        SearchHits<OrganizationDocument> searchHits =
                elasticsearchOperations.search(query, OrganizationDocument.class);

        // 검색 결과를 목록 DTO로 변환
        List<OrganizationListDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(document -> OrganizationListDto.builder()
                        .organizationPublicId(document.getPublicId())
                        .organizationType(document.getOrganizationType())
                        .organizationName(document.getOrganizationName())
                        .contactFirstName(document.getContactFirstName())
                        .contactMiddleName(document.getContactMiddleName())
                        .contactLastName(document.getContactLastName())
                        .contactEmail(document.getContactEmail())
                        .contactPhone(document.getContactPhone())
                        .status(document.getStatus())
                        .build())
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 전체 조직 초기 색인
    public void reindexAllOrganizations() {
        organizationRepository.findAll().forEach(organization ->
                organizationSearchRepository.save(OrganizationDocument.fromEntity(organization))
        );
    }
}
