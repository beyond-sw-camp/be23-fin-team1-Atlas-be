package com.ozz.atlas.supply.returns.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.dtos.ReturnItemResponseDto;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.returns.search.document.ReturnDocument;
import com.ozz.atlas.supply.returns.search.dtos.ReturnSearchDto;
import com.ozz.atlas.supply.returns.search.repository.ReturnSearchRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
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
public class ReturnSearchService {

    private final ReturnSearchRepository returnSearchRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final com.ozz.atlas.supply.settlement.repository.SettlementRepository settlementRepository;

    // 반품 생성/수정/상태변경 후 ES 문서를 저장
    @Transactional
    public void saveReturnDocument(ReturnRequest returnRequest) {
        returnSearchRepository.save(ReturnDocument.fromEntity(returnRequest));
    }

    // 검색 조건이 하나라도 있으면 ES 경로로 분기할 때 사용
    public boolean hasSearchCondition(ReturnSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || hasText(searchDto.getRequestOrganizationPublicId())
                        || hasText(searchDto.getTargetOrganizationPublicId())
                        || hasText(searchDto.getSourceShipmentPublicId())
                        || searchDto.getReturnType() != null
                        || searchDto.getResolutionType() != null
                        || searchDto.getReturnStatus() != null
                        || hasText(searchDto.getItemPublicId())
        );
    }

    // 반품 목록 검색
    public Page<ReturnRequestResponseDto> search(
            Pageable pageable,
            ReturnSearchDto searchDto,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateReturnSearchActor(organizationPublicId, organizationType, userRole);

        if (searchDto == null) {
            searchDto = new ReturnSearchDto();
        }

        // 검색어 조건(must)
        List<Query> mustQueries = new ArrayList<>();

        // 필터 조건(filter)
        List<Query> filterQueries = new ArrayList<>();

        filterQueries.add(Query.of(q -> q.bool(b -> b
                .should(s -> s.term(t -> t
                        .field("requestOrganizationPublicId")
                        .value(organizationPublicId)
                ))
                .should(s -> s.term(t -> t
                        .field("targetOrganizationPublicId")
                        .value(organizationPublicId)
                ))
                .minimumShouldMatch("1")
        )));

        // 제외 조건(mustNot)
        List<Query> mustNotQueries = new ArrayList<>();

        // 요청 조직 필터
        if (hasText(searchDto.getRequestOrganizationPublicId())) {
            filterQueries.add(termQuery("requestOrganizationPublicId", searchDto.getRequestOrganizationPublicId()));
        }

        // 대상 조직 필터
        if (hasText(searchDto.getTargetOrganizationPublicId())) {
            filterQueries.add(termQuery("targetOrganizationPublicId", searchDto.getTargetOrganizationPublicId()));
        }

        // 원출하 필터
        if (hasText(searchDto.getSourceShipmentPublicId())) {
            filterQueries.add(termQuery("sourceShipmentPublicId", searchDto.getSourceShipmentPublicId()));
        }

        // 반품 유형 필터
        if (searchDto.getReturnType() != null) {
            filterQueries.add(termQuery("returnType", searchDto.getReturnType().name()));
        }

        // 반품 상태 필터
        if (searchDto.getReturnStatus() != null) {
            filterQueries.add(termQuery("returnStatus", searchDto.getReturnStatus().name()));
        }

        // 품목 은 items nested 필드 기준으로 필터링
        Query itemNestedQuery = buildItemNestedQuery(searchDto);
        if (itemNestedQuery != null) {
            filterQueries.add(itemNestedQuery);
        }

        // 키워드가 있으면 반품번호 / 반품사유 / 상세사유 기준으로 검색
        if (hasText(searchDto.getKeyword())) {
            mustQueries.add(buildKeywordQuery(searchDto.getKeyword()));
        }

        // 최종 bool 쿼리 조립
        Query finalQuery = buildFinalQuery(mustQueries, filterQueries, mustNotQueries);

        // 페이지 정보까지 포함해서 ES 쿼리 생성
        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        // ES 검색 실행
        SearchHits<ReturnDocument> searchHits =
                elasticsearchOperations.search(query, ReturnDocument.class);

        // 검색 결과 문서를 기존 응답 DTO 형태로 변환
        List<ReturnRequestResponseDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponseDto)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스 초기화용 전체 재색인
    @Transactional
    public void reindexAllReturns() {
        returnRequestRepository.findAll().forEach(this::saveReturnDocument);
    }

    // 반품번호 / 반품사유 / 상세사유 검색
    private Query buildKeywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.match(m -> m
                        .field("returnNumber.ngram")
                        .query(keyword)
                ))
                .should(s -> s.match(m -> m
                        .field("returnReason.ngram")
                        .query(keyword)
                ))
                .should(s -> s.nested(n -> n
                        .path("items")
                        .query(nq -> nq.match(m -> m
                                .field("items.detailReason.ngram")
                                .query(keyword)
                        ))
                ))
                .minimumShouldMatch("1")
        ));
    }

    // 품목 publicId 를 items nested 필드 기준으로 필터링
    private Query buildItemNestedQuery(ReturnSearchDto searchDto) {
        List<Query> itemFilters = new ArrayList<>();

        // 특정 품목 기준 필터
        if (hasText(searchDto.getItemPublicId())) {
            itemFilters.add(termQuery("items.itemPublicId", searchDto.getItemPublicId()));
        }

        // 조건이 없으면 nested 쿼리를 만들 필요가 없음
        if (itemFilters.isEmpty()) {
            return null;
        }

        return Query.of(q -> q.nested(n -> n
                .path("items")
                .query(Query.of(nq -> nq.bool(b -> b.filter(itemFilters))))
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

    // ES 문서를 기존 반품 응답 DTO 형태로 바꿈
    private ReturnRequestResponseDto toResponseDto(ReturnDocument document) {
        String reqOrgName = null;
        if (document.getRequestOrganizationPublicId() != null) {
            SupplySupplier reqSupplier = supplierRepository.findByOrganizationPublicId(document.getRequestOrganizationPublicId()).orElse(null);
            if (reqSupplier != null) {
                reqOrgName = reqSupplier.getSupplierName();
            }
        }

        String tgtOrgName = null;
        if (document.getTargetOrganizationPublicId() != null) {
            SupplySupplier tgtSupplier = supplierRepository.findByOrganizationPublicId(document.getTargetOrganizationPublicId()).orElse(null);
            if (tgtSupplier != null) {
                tgtOrgName = tgtSupplier.getSupplierName();
            }
        }

        String settlementPublicId = settlementRepository.findByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                com.ozz.atlas.supply.settlement.domain.SettlementTargetType.RETURN,
                document.getPublicId(),
                com.ozz.atlas.supply.settlement.domain.SettlementStatus.CANCELLED
        ).map(com.ozz.atlas.supply.settlement.domain.Settlement::getPublicId).orElse(null);

        return ReturnRequestResponseDto.builder()
                .id(document.getId())
                .publicId(document.getPublicId())
                .returnNumber(document.getReturnNumber())
                .sourceShipmentPublicId(document.getSourceShipmentPublicId())
                .returnShipmentPublicId(document.getReturnShipmentPublicId())
                .exchangeShipmentPublicId(document.getExchangeShipmentPublicId())
                .requestOrganizationPublicId(document.getRequestOrganizationPublicId())
                .targetOrganizationPublicId(document.getTargetOrganizationPublicId())
                .requestOrganizationName(reqOrgName)
                .targetOrganizationName(tgtOrgName)
                .returnType(document.getReturnType())
                .resolutionType(document.getResolutionType())
                .returnReason(document.getReturnReason())
                .returnStatus(document.getReturnStatus())
                .requestedAt(document.getRequestedAt())
                .approvedAt(document.getApprovedAt())
                .completedAt(document.getCompletedAt())
                .createdByUserPublicId(document.getCreatedByUserPublicId())
                .settlementPublicId(settlementPublicId)
                // 첨부파일 목록이 null이면 빈 리스트로 내려줌
                .attachmentPublicIds(
                        document.getAttachmentPublicIds() == null ? List.of() : document.getAttachmentPublicIds()
                )
                .items(
                        document.getItems() == null
                                ? List.of()
                                : document.getItems().stream()
                                // ReturnItemResponseDto는 별도 DTO 클래스
                                .<ReturnItemResponseDto>map(item -> {
                                    String itemName = null;
                                    if (item.getItemPublicId() != null) {
                                        SupplyItem supplyItem = supplyItemRepository.findByPublicId(item.getItemPublicId()).orElse(null);
                                        if (supplyItem != null) {
                                            itemName = supplyItem.getItemName();
                                        }
                                    }
                                    return ReturnItemResponseDto.builder()
                                        .id(item.getId())
                                        .itemPublicId(item.getItemPublicId())
                                        .itemName(itemName)
                                        .returnQty(item.getReturnQty())
                                        .unit(item.getUnit())
                                        .detailReason(item.getDetailReason())
                                        .itemStatus(item.getItemStatus())
                                        .qcStatus(item.getQcStatus())
                                        .qcGrade(item.getQcGrade())
                                        .disposalReason(item.getDisposalReason())
                                        .disposalProofAttachmentPublicId(item.getDisposalProofAttachmentPublicId())
                                        .attachmentPublicIds(
                                                item.getAttachmentPublicIds() == null ? List.of() : item.getAttachmentPublicIds()
                                        )
                                        .build();
                                })

                                .toList()
                )
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

    private void validateReturnSearchActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank()
                || organizationType == null || organizationType.isBlank()) {
            throw new com.ozz.atlas.supply.returns.exception.ReturnException(
                    com.ozz.atlas.supply.returns.exception.ReturnErrorCode.INVALID_RETURN_REQUEST
            );
        }

        if ("ADMIN".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(userRole)) {
            throw new com.ozz.atlas.supply.returns.exception.ReturnException(
                    com.ozz.atlas.supply.returns.exception.ReturnErrorCode.FORBIDDEN_RETURN_CREATE
            );
        }

        if (!"BUYER".equalsIgnoreCase(organizationType)
                && !"SUPPLIER".equalsIgnoreCase(organizationType)) {
            throw new com.ozz.atlas.supply.returns.exception.ReturnException(
                    com.ozz.atlas.supply.returns.exception.ReturnErrorCode.FORBIDDEN_RETURN_CREATE
            );
        }
    }

}
