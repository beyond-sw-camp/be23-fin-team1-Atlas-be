package com.ozz.atlas.supply.item.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.item.search.document.ItemDocument;
import com.ozz.atlas.supply.item.search.dtos.ItemSearchDto;
import com.ozz.atlas.supply.item.search.repository.ItemSearchRepository;
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
public class ItemSearchService {

    private final ItemSearchRepository itemSearchRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 품목 생성/수정/상태변경 후 ES 문서를 저장
    @Transactional
    public void saveItemDocument(SupplyItem item) {
        itemSearchRepository.save(ItemDocument.fromEntity(item));
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(ItemSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || hasText(searchDto.getSupplierPublicId())
                        || hasText(searchDto.getSupplierOrganizationPublicId())
                        || hasText(searchDto.getItemCategoryPublicId())
                        || searchDto.getStatus() != null
        );
    }

    // 품목 목록 검색
    public Page<ItemResponse> search(Pageable pageable, ItemSearchDto searchDto) {
        validateSearchRequest(searchDto);

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();


        // 상태 필터가 있으면 그 값으로 조회
        if (searchDto.getStatus() != null) {
            if (searchDto.getStatus() == Status.DELETE) {
                throw new ItemException(ItemErrorCode.INVALID_INPUT_VALUE);
            }
            filterQueries.add(termQuery("status", searchDto.getStatus().name()));
        } else {
            filterQueries.add(termQuery("status", Status.ACTIVE.name()));
        }



        // 공급사 publicId 필터
        if (hasText(searchDto.getSupplierPublicId())) {
            filterQueries.add(termQuery("supplierPublicId", searchDto.getSupplierPublicId()));
        }

        // 공급사 조직 publicId 필터
        if (hasText(searchDto.getSupplierOrganizationPublicId())) {
            filterQueries.add(termQuery("supplierOrganizationPublicId", searchDto.getSupplierOrganizationPublicId()));
        }

        // 카테고리 필터
        if (hasText(searchDto.getItemCategoryPublicId())) {
            filterQueries.add(termQuery("itemCategoryPublicId", searchDto.getItemCategoryPublicId()));
        }


        // 키워드 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries, mustNotQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ItemDocument> searchHits =
                elasticsearchOperations.search(query, ItemDocument.class);

        List<ItemResponse> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toItemResponse)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllItems() {
        supplyItemRepository.findAll().forEach(this::saveItemDocument);
    }

    // 품목코드 / 품목명 / 공급사명 / 카테고리명 / 규격 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("itemCode.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("itemName.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("supplierName.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("categoryName.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("spec.ngram")
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

    private ItemResponse toItemResponse(ItemDocument document) {
        return ItemResponse.builder()
                .publicId(document.getPublicId())
                .supplierPublicId(document.getSupplierPublicId())
                .supplierOrganizationPublicId(document.getSupplierOrganizationPublicId())
                .supplierName(document.getSupplierName())
                .itemCategoryPublicId(document.getItemCategoryPublicId())
                .categoryName(document.getCategoryName())
                .itemCode(document.getItemCode())
                .itemName(document.getItemName())
                .unit(document.getUnit() != null ? document.getUnit().name() : null)
                .spec(document.getSpec())
                .shelfLifeDays(document.getShelfLifeDays())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .unitPrice(document.getUnitPrice())
                .build();
    }

    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    private void validateSearchRequest(ItemSearchDto searchDto) {
        if (searchDto == null) {
            throw new ItemException(ItemErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
