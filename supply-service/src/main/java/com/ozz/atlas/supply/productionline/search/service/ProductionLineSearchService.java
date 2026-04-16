package com.ozz.atlas.supply.productionline.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.repository.ProductionLineRepository;
import com.ozz.atlas.supply.productionline.search.document.ProductionLineDocument;
import com.ozz.atlas.supply.productionline.search.dtos.ProductionLineSearchDto;
import com.ozz.atlas.supply.productionline.search.repository.ProductionLineSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionLineSearchService {

    private final ProductionLineSearchRepository productionLineSearchRepository;
    private final ProductionLineRepository productionLineRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 생산라인 생성/수정/상태변경 후 ES 문서를 저장
    @Transactional
    public void saveProductionLineDocument(ProductionLine productionLine) {
        productionLineSearchRepository.save(ProductionLineDocument.fromEntity(productionLine));
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(ProductionLineSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || hasText(searchDto.getLogisticsNodePublicId())
                        || hasText(searchDto.getLineType())
                        || searchDto.getStatus() != null
        );
    }

    // 생산라인 목록 검색
    public Page<ProductionLineResponseDto> search(Pageable pageable, ProductionLineSearchDto searchDto) {
        // null 이 들어와도 안전하게 빈 DTO로 바꿈
        if (searchDto == null) {
            searchDto = new ProductionLineSearchDto();
        }

        // 기존 목록 API는 삭제 상태를 노출하지 않았으므로 ES 검색도 막음
        if (searchDto.getStatus() == Status.DELETE) {
            throw new IllegalArgumentException("삭제된 생산라인은 검색할 수 없습니다.");
        }

        // 검색어 조건(must)
        List<Query> mustQueries = new ArrayList<>();

        // 필터 조건(filter)
        List<Query> filterQueries = new ArrayList<>();

        // 제외 조건(mustNot)
        List<Query> mustNotQueries = new ArrayList<>();

        // 물류 노드 publicId 필터
        if (hasText(searchDto.getLogisticsNodePublicId())) {
            filterQueries.add(termQuery("logisticsNodePublicId", searchDto.getLogisticsNodePublicId()));
        }

        // 생산라인 유형 필터
        if (hasText(searchDto.getLineType())) {
            filterQueries.add(termQuery("lineType.keyword", searchDto.getLineType()));
        }

        // 기존 목록 API와 동일하게, 상태 조건이 없으면 ACTIVE만 조회
        if (searchDto.getStatus() != null) {
            filterQueries.add(termQuery("status", searchDto.getStatus().name()));
        } else {
            filterQueries.add(termQuery("status", Status.ACTIVE.name()));
        }

        // 키워드가 있으면 코드, 이름, 유형 기준으로 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        // 최종 bool 쿼리를 조립
        Query finalQuery = buildFinalQuery(mustQueries, filterQueries, mustNotQueries);

        Pageable searchPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                convertSort(pageable.getSort())
        );

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(searchPageable)
                .build();


        SearchHits<ProductionLineDocument> searchHits =
                elasticsearchOperations.search(query, ProductionLineDocument.class);

        List<ProductionLineResponseDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponseDto)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllProductionLines() {
        productionLineRepository.findAll().forEach(this::saveProductionLineDocument);
    }

    // 생산라인 코드, 이름, 유형 키워드 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("lineCode.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("lineName.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("lineType.ngram")
                        .query(keyword)
                ))
                .minimumShouldMatch("1")
        ));
    }

    // must / filter / mustNot 조건을 최종 bool 쿼리로 조립
    private Query buildFinalQuery(List<Query> mustQueries, List<Query> filterQueries, List<Query> mustNotQueries) {
        if (mustQueries.isEmpty() && filterQueries.isEmpty() && mustNotQueries.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        return Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            if (!mustNotQueries.isEmpty()) {
                b.mustNot(mustNotQueries);
            }
            return b;
        }));
    }

    // ES 문서를 기존 생산라인 응답 DTO 형태로 바꿈
    private ProductionLineResponseDto toResponseDto(ProductionLineDocument document) {
        return ProductionLineResponseDto.builder()
                .productionLineId(document.getId())
                .logisticsNodePublicId(document.getLogisticsNodePublicId())
                .lineCode(document.getLineCode())
                .lineName(document.getLineName())
                .lineType(document.getLineType())
                .status(document.getStatus())
                .dailyCapacity(document.getDailyCapacity())
                .build();
    }

    // 문자열 exact 필터용 term query
    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    // 공백이 아닌 문자열인지 확인
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Sort convertSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        List<Sort.Order> orders = sort.stream()
                .map(order -> {
                    String property = order.getProperty();

                    if ("productionLineId".equals(property)) {
                        return new Sort.Order(order.getDirection(), "id");
                    }

                    return order;
                })
                .toList();

        return Sort.by(orders);
    }

}
