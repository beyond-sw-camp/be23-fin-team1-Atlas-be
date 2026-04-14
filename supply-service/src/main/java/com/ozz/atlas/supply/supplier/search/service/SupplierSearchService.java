package com.ozz.atlas.supply.supplier.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import com.ozz.atlas.supply.supplier.esg.repository.SupplyEsgAssessmentRepository;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.document.SupplierDocument;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.search.repository.SupplierSearchRepository;
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
@Transactional
public class SupplierSearchService {

    private final SupplierSearchRepository supplierSearchRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierItemCapabilityRepository supplierItemCapabilityRepository;
    private final SupplierCertificateRepository supplierCertificateRepository;
    private final SupplyEsgAssessmentRepository supplyEsgAssessmentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 협력사 정보를 ES 문서로 저장
    public void saveSupplierDocument(SupplySupplier supplier) {

        // 협력사의 품목별 공급 역량 목록 조회
        List<SupplySupplierItemCapability> capabilities =
                supplierItemCapabilityRepository.findAllBySupplier_IdOrderByItem_ItemNameAsc(supplier.getId());

        // 협력사의 인증서 목록 조회
        List<SupplierCertificate> certificates =
                supplierCertificateRepository.findBySupplierPublicId(supplier.getPublicId());

        // 협력사의 최신 ESG 평가 1건 조회
        SupplyEsgAssessment latestEsgAssessment =
                supplyEsgAssessmentRepository.findTopBySupplier_IdOrderByEvaluatedAtDesc(supplier.getId())
                        .orElse(null);

        // 협력사 + 역량 + 인증서 + ESG 정보를 하나의 ES 문서로 변환
        SupplierDocument supplierDocument = SupplierDocument.fromEntity(
                supplier,
                capabilities,
                certificates,
                latestEsgAssessment
        );

        // ES에 저장
        supplierSearchRepository.save(supplierDocument);
    }

    // 협력사 통합검색 실행
    @Transactional(readOnly = true)
    public Page<SupplierResponse> search(Pageable pageable, SupplierSearchDto searchDto) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filters = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();

        // 기본 키워드가 있으면 협력사 기본 정보 대상으로 검색
        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isBlank()) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(searchDto.getKeyword())
                    // supplierName.ngram 덕분에 "한결", "푸드" 같은 중간 검색도 가능
                    .fields(List.of(
                            "supplierName^3.0",
                            "supplierName.ngram^2.0",
                            "supplierCode^2.0",
                            "primaryContactName",
                            "primaryContactEmail",
                            "primaryContactPhone"
                    ))
            )));
        }


        // 승인 상태가 없으면 기본적으로 승인 완료된 협력사만 검색
        ApprovalStatus approvalStatus = searchDto.getApprovalStatus() != null
                ? searchDto.getApprovalStatus()
                : ApprovalStatus.APPROVED;
        filters.add(termQuery("approvalStatus.keyword", approvalStatus.name()));

        // 협력사 상태가 있으면 그 상태만 조회
        if (searchDto.getSupplierStatus() != null) {
            filters.add(termQuery("supplierStatus.keyword", searchDto.getSupplierStatus().name()));
        } else {
            // 상태 조건이 없으면 종료된 협력사는 기본적으로 제외
            mustNotQueries.add(termQuery("supplierStatus.keyword", SupplierStatus.TERMINATED.name()));
        }

        // 협력사 단계 조건
        if (searchDto.getTierLevel() != null) {
            filters.add(termQuery("tierLevel", searchDto.getTierLevel().name()));
        }

        // 특정 조직 소속 협력사만 조회
        if (searchDto.getOrganizationPublicId() != null && !searchDto.getOrganizationPublicId().isBlank()) {
            filters.add(termQuery("organizationPublicId.keyword", searchDto.getOrganizationPublicId()));
        }

        // ESG 등급 조건
        if (searchDto.getEsgGrade() != null) {
            filters.add(termQuery("esgGrade.keyword", searchDto.getEsgGrade().name()));
        }

        // ESG 총점 하한선 조건
        if (searchDto.getMinTotalScore() != null) {
            filters.add(gteNumberRangeQuery("totalScore", searchDto.getMinTotalScore().doubleValue()));
        }

        // 품목 역량 조건이 있으면 nested 쿼리 생성
        Query capabilityNestedQuery = buildCapabilityNestedQuery(searchDto);
        if (capabilityNestedQuery != null) {
            filters.add(capabilityNestedQuery);
        }

        // 인증서 조건이 있으면 nested 쿼리 생성
        Query certificateNestedQuery = buildCertificateNestedQuery(searchDto);
        if (certificateNestedQuery != null) {
            filters.add(certificateNestedQuery);
        }

        // must + filter + mustNot 조건을 하나의 최종 bool 쿼리로 묶음
        Query finalQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filters.isEmpty()) {
                b.filter(filters);
            }
            if (!mustNotQueries.isEmpty()) {
                b.mustNot(mustNotQueries);
            }
            return b;
        }));

        // 페이징 정보까지 포함해서 ES 쿼리 생성
        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        // ES 검색 실행
        SearchHits<SupplierDocument> searchHits =
                elasticsearchOperations.search(query, SupplierDocument.class);

        // 검색 결과 문서를 협력사 응답 DTO로 변환
        List<SupplierResponse> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toSupplierResponse)
                .toList();

        // Page 형태로 반환
        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 품목별 공급 역량 조건을 nested 쿼리로 생성
    private Query buildCapabilityNestedQuery(SupplierSearchDto searchDto) {
        List<Query> capabilityFilters = new ArrayList<>();

        // 특정 품목을 공급할 수 있는 협력사 조건
        if (searchDto.getItemPublicId() != null && !searchDto.getItemPublicId().isBlank()) {
            capabilityFilters.add(termQuery("capabilities.itemPublicId.keyword", searchDto.getItemPublicId()));
        }

        // 월 생산 가능 수량 하한선 조건
        if (searchDto.getMinMonthlyCapacity() != null) {
            capabilityFilters.add(gteNumberRangeQuery(
                    "capabilities.monthlyCapacity",
                    searchDto.getMinMonthlyCapacity().doubleValue()
            ));
        }

        // 현재 공급 가능 수량 하한선 조건
        if (searchDto.getMinAvailableQty() != null) {
            capabilityFilters.add(gteNumberRangeQuery(
                    "capabilities.availableQty",
                    searchDto.getMinAvailableQty().doubleValue()
            ));
        }

        // MOQ 상한선 조건
        if (searchDto.getMaxMoq() != null) {
            capabilityFilters.add(lteNumberRangeQuery(
                    "capabilities.moq",
                    searchDto.getMaxMoq().doubleValue()
            ));
        }

        // 리드타임 상한선 조건
        if (searchDto.getMaxLeadTimeDays() != null) {
            capabilityFilters.add(lteNumberRangeQuery(
                    "capabilities.leadTimeDays",
                    searchDto.getMaxLeadTimeDays().doubleValue()
            ));
        }

        // 품질 등급 조건
        if (searchDto.getQualityGrade() != null) {
            capabilityFilters.add(termQuery(
                    "capabilities.qualityGrade.keyword",
                    searchDto.getQualityGrade().name()
            ));
        }

        // 품목 역량 조건이 하나도 없으면 nested 쿼리 자체를 만들지 않음
        if (capabilityFilters.isEmpty()) {
            return null;
        }

        // 같은 capability 한 건 안에서 조건이 함께 맞아야 하므로 nested로 묶음
        return Query.of(q -> q.nested(n -> n
                .path("capabilities")
                .query(Query.of(nq -> nq.bool(b -> b.filter(capabilityFilters))))
        ));
    }

    // 인증서 조건을 nested 쿼리로 생성
    private Query buildCertificateNestedQuery(SupplierSearchDto searchDto) {
        List<Query> certificateFilters = new ArrayList<>();

        // 특정 인증서 종류 조건
        if (searchDto.getCertificateTypeId() != null) {
            certificateFilters.add(termQuery(
                    "certificates.certificateTypeId",
                    searchDto.getCertificateTypeId()
            ));
        }

        // 인증서 상태 조건
        if (searchDto.getCertificateStatus() != null) {
            certificateFilters.add(termQuery(
                    "certificates.certificateStatus.keyword",
                    searchDto.getCertificateStatus().name()
            ));
        }

        // 현재 유효한 인증서만 가진 협력사 조건
        if (Boolean.TRUE.equals(searchDto.getOnlyValidCertificate())) {
            certificateFilters.add(termQuery("certificates.valid", true));
        }

        // 인증서 조건이 하나도 없으면 nested 쿼리 자체를 만들지 않음
        if (certificateFilters.isEmpty()) {
            return null;
        }

        // 같은 인증서 한 건 안에서 조건이 함께 맞아야 하므로 nested로 묶음
        return Query.of(q -> q.nested(n -> n
                .path("certificates")
                .query(Query.of(nq -> nq.bool(b -> b.filter(certificateFilters))))
        ));
    }

    // ES 문서를 기존 협력사 응답 DTO 형태로 변환
    private SupplierResponse toSupplierResponse(SupplierDocument document) {
        return SupplierResponse.builder()
                .publicId(document.getPublicId())
                .organizationPublicId(document.getOrganizationPublicId())
                .supplierCode(document.getSupplierCode())
                .supplierName(document.getSupplierName())
                .tierLevel(document.getTierLevel())
                .supplierStatus(document.getSupplierStatus())
                .approvalStatus(document.getApprovalStatus())
                .primaryContactName(document.getPrimaryContactName())
                .primaryContactEmail(document.getPrimaryContactEmail())
                .primaryContactPhone(document.getPrimaryContactPhone())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    // 문자열 값에 대한 term 쿼리 생성
    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    // 숫자 값에 대한 term 쿼리 생성
    private Query termQuery(String field, Long value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    // boolean 값에 대한 term 쿼리 생성
    private Query termQuery(String field, Boolean value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    // 숫자 이상 조건 range 쿼리 생성
    private Query gteNumberRangeQuery(String field, Double value) {
        return Query.of(q -> q.range(r -> r.number(n -> n
                .field(field)
                .gte(value)
        )));
    }

    // 숫자 이하 조건 range 쿼리 생성
    private Query lteNumberRangeQuery(String field, Double value) {
        return Query.of(q -> q.range(r -> r.number(n -> n
                .field(field)
                .lte(value)
        )));
    }

    // 협력사 문서를 ES에서 삭제
    public void deleteSupplierDocument(Long supplierId) {
        supplierSearchRepository.deleteById(supplierId);
    }

    // DB에 있는 전체 협력사를 ES에 다시 저장
    public void reindexAllSuppliers() {
        supplierRepository.findAll().forEach(this::saveSupplierDocument);
    }
}
