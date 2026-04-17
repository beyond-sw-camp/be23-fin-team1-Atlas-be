package com.ozz.atlas.supply.lot.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.dtos.LotResponseDto;
import com.ozz.atlas.supply.lot.repository.LotRepository;
import com.ozz.atlas.supply.lot.search.document.LotDocument;
import com.ozz.atlas.supply.lot.search.dtos.LotSearchDto;
import com.ozz.atlas.supply.lot.search.repository.LotSearchRepository;
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
public class LotSearchService {

    private final LotSearchRepository lotSearchRepository;
    private final LotRepository lotRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final com.ozz.atlas.supply.item.repository.SupplyItemRepository supplyItemRepository;

    // LOT 생성/수정/상태변경 후 ES 문서를 저장
    @Transactional
    public void saveLotDocument(Lot lot) {
        String supplierName = null;
        String itemName = null;
        if (lot.getItemPublicId() != null) {
            com.ozz.atlas.supply.item.domain.SupplyItem item = supplyItemRepository.findByPublicId(lot.getItemPublicId()).orElse(null);
            if (item != null) {
                itemName = item.getItemName();
                if (item.getSupplier() != null) {
                    supplierName = item.getSupplier().getSupplierName();
                }
            }
        }
        lotSearchRepository.save(LotDocument.fromEntity(lot, supplierName, itemName));
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(LotSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || hasText(searchDto.getSupplierPublicId())
                        || hasText(searchDto.getItemPublicId())
                        || hasText(searchDto.getSourcePoItemPublicId())
                        || hasText(searchDto.getCurrentNodePublicId())
                        || searchDto.getLotStatus() != null
                        || searchDto.getQualityStatus() != null
        );
    }

    // LOT 목록 검색
    public Page<LotResponseDto> search(Pageable pageable, LotSearchDto searchDto) {
        // null 이 들어와도 안전하게 빈 DTO로 바꿈
        if (searchDto == null) {
            searchDto = new LotSearchDto();
        }

        // 검색어 조건(must)
        List<Query> mustQueries = new ArrayList<>();

        // 필터 조건(filter)
        List<Query> filterQueries = new ArrayList<>();

        // 제외 조건(mustNot)
        List<Query> mustNotQueries = new ArrayList<>();

        // 공급사 publicId 필터
        if (hasText(searchDto.getSupplierPublicId())) {
            filterQueries.add(termQuery("supplierPublicId", searchDto.getSupplierPublicId()));
        }

        // 품목 publicId 필터
        if (hasText(searchDto.getItemPublicId())) {
            filterQueries.add(termQuery("itemPublicId", searchDto.getItemPublicId()));
        }

        // 원본 발주 품목 publicId 필터
        if (hasText(searchDto.getSourcePoItemPublicId())) {
            filterQueries.add(termQuery("sourcePoItemPublicId", searchDto.getSourcePoItemPublicId()));
        }

        // 현재 노드 publicId 필터
        if (hasText(searchDto.getCurrentNodePublicId())) {
            filterQueries.add(termQuery("currentNodePublicId", searchDto.getCurrentNodePublicId()));
        }

        // LOT 상태 필터
        if (searchDto.getLotStatus() != null) {
            filterQueries.add(termQuery("lotStatus", searchDto.getLotStatus().name()));
        }

        // 품질 상태 필터
        if (searchDto.getQualityStatus() != null) {
            filterQueries.add(termQuery("qualityStatus", searchDto.getQualityStatus().name()));
        }

        // 키워드가 있으면 lotNumber 기준으로 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        // 최종 bool 쿼리를 조립
        Query finalQuery = buildFinalQuery(mustQueries, filterQueries, mustNotQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<LotDocument> searchHits =
                elasticsearchOperations.search(query, LotDocument.class);

        List<LotResponseDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponseDto)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllLots() {
        lotRepository.findAll().forEach(this::saveLotDocument);
    }

    // 현재는 LOT 번호 중심으로 키워드 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("lotNumber.ngram")
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

    // ES 문서를 기존 LOT 응답 DTO 형태로 바꿈
    private LotResponseDto toResponseDto(LotDocument document) {
        return LotResponseDto.builder()
                .publicId(document.getPublicId())
                .lotNumber(document.getLotNumber())
                .sourcePoItemPublicId(document.getSourcePoItemPublicId())
                .supplierPublicId(document.getSupplierPublicId())
                .itemPublicId(document.getItemPublicId())
                .supplierName(document.getSupplierName())
                .itemName(document.getItemName())
                .lotStatus(document.getLotStatus())
                .manufacturedAt(document.getManufacturedAt())
                .expiredAt(document.getExpiredAt())
                .qty(document.getQty())
                .unit(document.getUnit())
                .qualityStatus(document.getQualityStatus())
                .currentNodePublicId(document.getCurrentNodePublicId())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
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
}
