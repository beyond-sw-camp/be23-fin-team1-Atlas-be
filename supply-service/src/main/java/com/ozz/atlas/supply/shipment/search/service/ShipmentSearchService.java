package com.ozz.atlas.supply.shipment.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.search.document.ShipmentDocument;
import com.ozz.atlas.supply.shipment.search.dtos.ShipmentSearchDto;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.shipment.search.repository.ShipmentSearchRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentSearchService {

    private final ShipmentSearchRepository shipmentSearchRepository;
    private final ShipmentRepository shipmentRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 출하 엔티티를 Elasticsearch 문서로 저장
    // 생성, 트래킹, 상태 변경 등 출하 핵심 정보가 바뀔 때마다 재저장
    @Transactional
    public void saveShipmentDocument(Shipment shipment) {
        LogisticsNode originNode = findNode(shipment.getOriginNodeId()).orElse(null);
        LogisticsNode destinationNode = findNode(shipment.getDestinationNodeId()).orElse(null);
        LogisticsNode currentNode = findNode(shipment.getCurrentNodeId()).orElse(null);

        shipmentSearchRepository.save(
                ShipmentDocument.fromEntity(shipment, originNode, destinationNode, currentNode)
        );
    }

    // 검색 조건이 하나라도 있으면 ES 검색으로 분기
    public boolean hasSearchCondition(ShipmentSearchDto searchDto) {
        return searchDto != null && (
                hasText(searchDto.getKeyword())
                        || searchDto.getStatus() != null
                        || hasText(searchDto.getOriginNodePublicId())
                        || hasText(searchDto.getDestinationNodePublicId())
                        || hasText(searchDto.getCurrentNodePublicId())
                        || searchDto.getTemperatureRequired() != null
        );
    }

    // 출하 통합검색
    // keyword 는 출하번호, 송장번호, 운송사명, 차량번호, 노드명 기준으로 검색
    public Page<ShipmentListResponseDto> search(Pageable pageable, ShipmentSearchDto searchDto) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        if (searchDto != null && hasText(searchDto.getKeyword())) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(searchDto.getKeyword())
                    .fields(List.of(
                            "shipmentNumber^4.0",
                            "shipmentNumber.ngram^3.0",
                            "trackingNo^3.0",
                            "trackingNo.ngram^2.0",
                            "carrierName^2.0",
                            "carrierName.ngram^2.0",
                            "vehicleNo^2.0",
                            "vehicleNo.ngram^2.0",
                            "originNodeName^2.0",
                            "originNodeName.ngram^2.0",
                            "destinationNodeName^2.0",
                            "destinationNodeName.ngram^2.0",
                            "currentNodeName^2.0",
                            "currentNodeName.ngram^2.0"
                    ))
            )));
        }

        if (searchDto != null && searchDto.getStatus() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("status")
                    .value(searchDto.getStatus().name())
            )));
        }

        if (searchDto != null && hasText(searchDto.getOriginNodePublicId())) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("originNodePublicId")
                    .value(searchDto.getOriginNodePublicId())
            )));
        }

        if (searchDto != null && hasText(searchDto.getDestinationNodePublicId())) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("destinationNodePublicId")
                    .value(searchDto.getDestinationNodePublicId())
            )));
        }

        if (searchDto != null && hasText(searchDto.getCurrentNodePublicId())) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("currentNodePublicId")
                    .value(searchDto.getCurrentNodePublicId())
            )));
        }

        if (searchDto != null && searchDto.getTemperatureRequired() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("temperatureRequired")
                    .value(searchDto.getTemperatureRequired())
            )));
        }

        Query finalQuery = buildFinalQuery(mustQueries, filterQueries);

        NativeQuery query = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ShipmentDocument> searchHits =
                elasticsearchOperations.search(query, ShipmentDocument.class);

        List<ShipmentListResponseDto> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toListResponse)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 인덱스가 비어 있거나 새로 만들어졌을 때 DB 기준으로 전체 재색인
    @Transactional
    public void reindexAllShipments() {
        shipmentRepository.findAll().forEach(this::saveShipmentDocument);
    }

    private ShipmentListResponseDto toListResponse(ShipmentDocument document) {
        return ShipmentListResponseDto.builder()
                .publicId(document.getPublicId())
                .shipmentNumber(document.getShipmentNumber())
                .carrierName(document.getCarrierName())
                .destinationNodePublicId(document.getDestinationNodePublicId())
                .currentNodePublicId(document.getCurrentNodePublicId())
                .arrivalEta(document.getArrivalEta())
                .status(document.getStatus())
                .build();
    }

    private Query buildFinalQuery(List<Query> mustQueries, List<Query> filterQueries) {
        if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        return Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) {
                b.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        }));
    }

    private Optional<LogisticsNode> findNode(Long nodeId) {
        if (nodeId == null) {
            return Optional.empty();
        }
        return logisticsNodeRepository.findById(nodeId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
