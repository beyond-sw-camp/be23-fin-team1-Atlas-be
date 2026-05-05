package com.ozz.atlas.auth.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.dtos.organization.OrganizationListDto;
import com.ozz.atlas.auth.search.dtos.OrganizationSearchDto;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.search.document.OrganizationDocument;
import com.ozz.atlas.auth.search.repository.OrganizationSearchRepository;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    // 조직 엔티티를 Elasticsearch 문서로 저장
    // 조직 등록, 수정, 삭제(상태 변경) 후 검색 문서를 계속 동기화할 때 사용
    public void saveOrganizationDocument(Organization organization) {
        organizationSearchRepository.save(OrganizationDocument.fromEntity(organization));
    }

    // 조직 통합검색
    public Page<OrganizationListDto> search(Pageable pageable, OrganizationSearchDto searchDto) {
        // mustQueries:
        // 실제로 검색어가 들어가는 조건들
        // 예: keyword, organizationName 같은 "무엇을 찾을지"에 대한 조건
        List<Query> mustQueries = new ArrayList<>();

        // filterQueries:
        // 결과를 좁히는 정확한 조건들
        // 예: organizationType, status 같은 정확 일치 필터
        List<Query> filterQueries = new ArrayList<>();

        // 조직 유형이 들어오면 정확히 같은 조직 유형만 필터링
        if (searchDto.getOrganizationType() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("organizationType.keyword")
                    .value(searchDto.getOrganizationType().name())
            )));
        }

        // 상태가 안 들어오면 기본적으로 ACTIVE 조직만 조회
        Status status = searchDto.getStatus() != null ? searchDto.getStatus() : Status.ACTIVE;
        filterQueries.add(Query.of(q -> q.term(t -> t
                .field("status.keyword")
                .value(status.name())
        )));

        // keyword는 조직 통합검색창 용도
        // 조직명, 사업자번호, 담당자 이름, 이메일, 전화번호를 한 번에 검색
        // ngram 필드를 같이 넣어서 부분검색도 가능하게 함
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(searchDto.getKeyword())
                    .fields(List.of(
                            "organizationName^3.0",
                            "organizationName.ngram^2.0",
                            "organizationEnglishName^3.0",
                            "organizationEnglishName.ngram^2.0",
                            "businessNo^2.0",
                            "businessNo.ngram^2.0",
                            "contactFirstName^2.0",
                            "contactFirstName.ngram^2.0",
                            "organizationAlias^3.0",
                            "organizationAlias.ngram^2.0",
                            "contactMiddleName",
                            "contactMiddleName.ngram",
                            "contactLastName^2.0",
                            "contactLastName.ngram^2.0",
                            "contactEmail^2.0",
                            "contactEmail.ngram^2.0",
                            "contactPhone",
                            "contactPhone.ngram^2.0",
                            "address^2.0",
                            "address.ngram^2.0",
                            "addressDetail^2.0",
                            "addressDetail.ngram^2.0",
                            "zipCode",
                            "zipCode.ngram"

                    ))
            )));
        }

        // organizationName 상세검색도 부분검색이 되도록 ngram 필드를 사용
        addPartialMatchIfPresent(mustQueries, "organizationName.ngram", searchDto.getOrganizationName());
        addPartialMatchIfPresent(mustQueries, "organizationEnglishName.ngram", searchDto.getOrganizationEnglishName());
        addPartialMatchIfPresent(mustQueries, "organizationAlias.ngram", searchDto.getOrganizationAlias());

        // 최종 bool 쿼리를 조립
        Query finalQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        }));

        // pageable을 그대로 넘겨서 페이지, 크기, 정렬 정보를 유지
        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        // Elasticsearch에 실제 검색을 요청
        SearchHits<OrganizationDocument> searchHits =
                elasticsearchOperations.search(query, OrganizationDocument.class);

        // 검색 결과 문서를 목록 DTO로 변환
        List<OrganizationListDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(document -> OrganizationListDto.builder()
                        // 프론트가 조직 상세 조회를 할 때 필요한 내부 ID입니다.
                        // 이 값이 없으면 프론트의 hasOrganizationId(row)가 false가 되어서 클릭이 막힙니다.
                        .organizationId(document.getOrganizationId())

                        // 화면 표시와 다른 API 연동에 쓰는 공개 ID입니다.
                        .organizationPublicId(document.getPublicId())
                        .organizationType(document.getOrganizationType())
                        .organizationName(document.getOrganizationName())
                        .organizationEnglishName(document.getOrganizationEnglishName())
                        .organizationAlias(document.getOrganizationAlias())
                        .contactFirstName(document.getContactFirstName())
                        .contactMiddleName(document.getContactMiddleName())
                        .contactLastName(document.getContactLastName())
                        .contactEmail(document.getContactEmail())
                        .contactPhone(document.getContactPhone())
                        .status(document.getStatus())
                        .organizationImageThumbPath(document.getOrganizationImageThumbPath())
                        .address(document.getAddress())
                        .addressDetail(document.getAddressDetail())
                        .zipCode(document.getZipCode())
                        .build())
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // DB의 조직 전체를 Elasticsearch에 다시 색인
    // organizations 인덱스를 새로 만들었을 때 초기 적재용으로 사용
    public void reindexAllOrganizations() {
        organizationRepository.findAll().forEach(organization ->
                organizationSearchRepository.save(OrganizationDocument.fromEntity(organization))
        );
    }

    // 문자열이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // 값이 들어왔을 때만 부분검색 match 쿼리를 추가
    // ngram 필드를 대상으로 검색하므로 일부 문자열만 입력해도 찾을 수 있음
    private void addPartialMatchIfPresent(List<Query> mustQueries, String field, String value) {
        if (!hasText(value)) {
            return;
        }

        mustQueries.add(Query.of(q -> q.match(m -> m
                .field(field)
                .query(value)
        )));
    }
}
