package com.ozz.atlas.supply.purchaseorder.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.dtos.PurchaseOrderSummaryResponse;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderErrorCode;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderException;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.purchaseorder.search.document.PurchaseOrderDocument;
import com.ozz.atlas.supply.purchaseorder.search.dtos.PurchaseOrderSearchDto;
import com.ozz.atlas.supply.purchaseorder.search.repository.PurchaseOrderSearchRepository;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderSearchService {

    private final PurchaseOrderSearchRepository purchaseOrderSearchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 발주 생성/수정/상태변경/소프트삭제 후 ES 문서를 다시 저장
    @Transactional
    public void savePurchaseOrderDocument(SupplyPurchaseOrder purchaseOrder) {
        purchaseOrderSearchRepository.save(PurchaseOrderDocument.fromEntity(purchaseOrder));
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(PurchaseOrderSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || searchDto.getPoStatus() != null
                        || hasText(searchDto.getSupplierPublicId())
        );
    }

    // 발주 목록 검색
    public Page<PurchaseOrderSummaryResponse> search(Pageable pageable, PurchaseOrderSearchDto searchDto) {
        validateSearchRequest(searchDto);

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();

        // 소프트 삭제된 발주는 항상 제외
        mustNotQueries.add(termQuery("poStatus", PoStatus.DELETED.name()));

        // BUYER 화면이면 발주사 조직 기준으로 제한
        if (searchDto.getViewType() == PurchaseOrderViewType.BUYER) {
            filterQueries.add(termQuery("buyerOrganizationPublicId", searchDto.getOrganizationPublicId()));
        }

        // SUPPLIER 화면이면 협력사 조직 기준으로 제한
        if (searchDto.getViewType() == PurchaseOrderViewType.SUPPLIER) {
            filterQueries.add(termQuery("supplierOrganizationPublicId", searchDto.getOrganizationPublicId()));
        }

        // 특정 협력사 필터
        if (hasText(searchDto.getSupplierPublicId())) {
            filterQueries.add(termQuery("supplierPublicId", searchDto.getSupplierPublicId()));
        }

        // 상태 필터
        if (searchDto.getPoStatus() != null) {
            filterQueries.add(termQuery("poStatus", searchDto.getPoStatus().name()));
        }

        // 키워드가 있으면 발주번호/협력사/메모/품목 기준으로 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries, mustNotQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<PurchaseOrderDocument> searchHits =
                elasticsearchOperations.search(query, PurchaseOrderDocument.class);

        List<PurchaseOrderSummaryResponse> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toSummaryResponse)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllPurchaseOrders() {
        purchaseOrderRepository.findAll().forEach(this::savePurchaseOrderDocument);
    }

    // 발주번호 / 협력사코드 / 협력사명 / 메모 / 품목코드 / 품목명 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("poNumber.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("supplierCode.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("supplierName.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("memo.ngram")
                        .query(keyword)
                ))
                .should(s -> s.nested(n -> n
                        .path("items")
                        .query(nq -> nq.match(m -> m
                                .field("items.itemCode.ngram")
                                .query(keyword)
                        ))
                ))
                .should(s -> s.nested(n -> n
                        .path("items")
                        .query(nq -> nq.match(m -> m
                                .field("items.itemName.ngram")
                                .query(keyword)
                        ))
                ))
                .minimumShouldMatch("1")
        ));
    }

    // must / filter / mustNot 를 기존 ES 서비스들처럼 마지막에 한 번에 조립
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

    // ES 문서를 기존 목록 응답 DTO로 변환
    private PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrderDocument document) {
        return PurchaseOrderSummaryResponse.builder()
                .poPublicId(document.getPublicId())
                .poNumber(document.getPoNumber())
                .buyerOrganizationPublicId(document.getBuyerOrganizationPublicId())
                .supplierPublicId(document.getSupplierPublicId())
                .supplierCode(document.getSupplierCode())
                .supplierName(document.getSupplierName())
                .poStatus(document.getPoStatus())
                .orderedAt(document.getOrderedAt())
                .dueDate(document.getDueDate())
                .totalAmount(document.getTotalAmount())
                .currencyCode(document.getCurrencyCode())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    // 문자열 exact filter 용 term query
    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    // 입력값 검증
    private void validateSearchRequest(PurchaseOrderSearchDto searchDto) {
        if (searchDto == null
                || !hasText(searchDto.getOrganizationPublicId())
                || searchDto.getViewType() == null) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        // BUYER 화면에서는 supplierPublicId 를 받지 않음
        if (searchDto.getViewType() == PurchaseOrderViewType.BUYER
                && hasText(searchDto.getSupplierPublicId())) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
