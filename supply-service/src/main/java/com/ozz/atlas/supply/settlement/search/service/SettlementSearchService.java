package com.ozz.atlas.supply.settlement.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementDetail;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import com.ozz.atlas.supply.settlement.dtos.SettlementDetailResponseDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.repository.SettlementDetailRepository;
import com.ozz.atlas.supply.settlement.repository.SettlementRepository;
import com.ozz.atlas.supply.settlement.search.document.SettlementDocument;
import com.ozz.atlas.supply.settlement.search.dtos.SettlementSearchDto;
import com.ozz.atlas.supply.settlement.search.repository.SettlementSearchRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementSearchService {

    private final SettlementSearchRepository settlementSearchRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 정산 생성/승인/취소 후 ES 문서를 저장
    @Transactional
    public void saveSettlementDocument(Settlement settlement) {
        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());
        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        settlementSearchRepository.save(
                SettlementDocument.fromEntity(settlement, supplierPublicId, details)
        );
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(SettlementSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || hasText(searchDto.getSupplierPublicId())
                        || searchDto.getTargetType() != null
                        || searchDto.getSettlementStatus() != null
                        || searchDto.getCurrencyCode() != null
        );
    }

    // 정산 목록 검색
    public Page<SettlementResponseDto> search(
            Pageable pageable,
            SettlementSearchDto searchDto,
            String organizationPublicId
    ) {
        if (searchDto == null) {
            searchDto = new SettlementSearchDto();
        }

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();
        filterQueries.add(readableOrganizationQuery(organizationPublicId));

        // 공급사 publicId 필터
        if (hasText(searchDto.getSupplierPublicId())) {
            filterQueries.add(termQuery("supplierPublicId", searchDto.getSupplierPublicId()));
        }

        // 정산 대상 유형 필터
        if (searchDto.getTargetType() != null) {
            filterQueries.add(termQuery("targetType", searchDto.getTargetType().name()));
        }

        // 정산 상태 필터
        if (searchDto.getSettlementStatus() != null) {
            filterQueries.add(termQuery("settlementStatus", searchDto.getSettlementStatus().name()));
        }

        // 통화 코드 필터
        if (searchDto.getCurrencyCode() != null) {
            filterQueries.add(termQuery("currencyCode", searchDto.getCurrencyCode().name()));
        }

        // 키워드는 targetPublicId 기준으로 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

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

        SearchHits<SettlementDocument> searchHits =
                elasticsearchOperations.search(query, SettlementDocument.class);

        List<SettlementResponseDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponseDto)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllSettlements() {
        settlementRepository.findAll().forEach(this::saveSettlementDocument);
    }

    // targetPublicId 키워드 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("targetPublicId.ngram")
                        .query(keyword)
                ))
                .minimumShouldMatch("1")
        ));
    }

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

    // ES 문서를 기존 정산 응답 DTO 형태로 바꿉니다.
    private SettlementResponseDto toResponseDto(SettlementDocument document) {
        return SettlementResponseDto.builder()
                .id(document.getId())
                .publicId(document.getPublicId())
                .supplierPublicId(document.getSupplierPublicId())
                .buyerOrganizationPublicId(document.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(document.getSupplierOrganizationPublicId())
                .targetType(document.getTargetType())
                .targetPublicId(document.getTargetPublicId())
                .settlementPeriodStart(document.getSettlementPeriodStart())
                .settlementPeriodEnd(document.getSettlementPeriodEnd())
                .amount(document.getAmount())
                .currencyCode(document.getCurrencyCode())
                .settlementStatus(document.getSettlementStatus())
                .settledAt(document.getSettledAt())
                .approvedByUserPublicId(document.getApprovedByUserPublicId())
                .cancelledAt(document.getCancelledAt())
                .cancelledByUserPublicId(document.getCancelledByUserPublicId())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .details(
                        document.getDetails() == null
                                ? List.of()
                                : document.getDetails().stream()
                                .map(detail -> SettlementDetailResponseDto.builder()
                                        .publicId(detail.getPublicId())
                                        .poItemId(detail.getPoItemId())
                                        .itemId(detail.getItemId())
                                        .qty(detail.getQty())
                                        .unitPrice(detail.getUnitPrice())
                                        .amount(detail.getAmount())
                                        .detailStatus(detail.getDetailStatus())
                                        .build())
                                .toList()
                )
                .build();
    }
    private Query readableOrganizationQuery(String organizationPublicId) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.term(t -> t
                        .field("buyerOrganizationPublicId")
                        .value(organizationPublicId)
                ))
                .should(s -> s.term(t -> t
                        .field("supplierOrganizationPublicId")
                        .value(organizationPublicId)
                ))
                .minimumShouldMatch("1")
        ));
    }

    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String getSupplierPublicId(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .map(SupplySupplier::getPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공급사입니다."));
    }

    private Sort convertSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Order.desc("id"));
        }

        List<Sort.Order> orders = sort.stream()
                .map(order -> {
                    String property = order.getProperty();

                    if ("id".equals(property)) {
                        return new Sort.Order(order.getDirection(), "id");
                    }

                    return order;
                })
                .toList();

        return Sort.by(orders);
    }
}
