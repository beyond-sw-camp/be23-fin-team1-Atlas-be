package com.ozz.atlas.supply.supplier.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.dtos.ConnectedSupplierDetailResponse;
import com.ozz.atlas.supply.supplier.dtos.ConnectedSupplierOrderResponse;
import com.ozz.atlas.supply.supplier.dtos.ConnectedSupplierSummaryResponse;
import com.ozz.atlas.supply.supplier.dtos.CreateSupplierRequest;
import com.ozz.atlas.supply.supplier.dtos.SupplierListResponse;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.exception.SupplierErrorCode;
import com.ozz.atlas.supply.supplier.exception.SupplierException;
import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import com.ozz.atlas.supply.supplier.relation.service.SupplierRelationService;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierSearchService supplierSearchService;
    private final SupplyItemRepository supplyItemRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SupplierRelationService supplierRelationService;
    private final ShipmentRepository shipmentRepository;

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String BUYER_ORGANIZATION_TYPE = "BUYER";
    private static final String SUPPLIER_ORGANIZATION_TYPE = "SUPPLIER";
    private final PurchaseOrderRepository purchaseOrderRepository;


    public SupplierResponse createSupplier(String userRole, CreateSupplierRequest request) {
        validateAdminCreate(userRole);

        if (supplierRepository.existsByOrganizationPublicId(request.getOrganizationPublicId())) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_ORGANIZATION_ALREADY_EXISTS);
        }

        if (supplierRepository.existsBySupplierCodeAndSupplierStatusNot(
                request.getSupplierCode(),
                SupplierStatus.TERMINATED
        )) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CODE_ALREADY_EXISTS);
        }

        SupplySupplier supplier = SupplySupplier.create(
                request.getOrganizationPublicId(),
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );

        // 직접 등록이니까 바로 승인 + 활성 처리
        supplier.activate();

        SupplySupplier savedSupplier = supplierRepository.save(supplier);
        supplierSearchService.saveSupplierDocument(savedSupplier);

        return SupplierResponse.fromEntity(savedSupplier);
    }


    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(
            String supplierPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        SupplySupplier targetSupplier = getSupplierOrThrow(supplierPublicId);

        if (isAdmin(userRole)) {
            return SupplierResponse.fromEntity(targetSupplier);
        }

        if (BUYER_ORGANIZATION_TYPE.equals(organizationType)) {
            validateOrganizationHeader(organizationPublicId);

            boolean hasRelation = purchaseOrderRepository.existsByBuyerOrganizationPublicIdAndSupplier_IdAndPoStatusNot(
                    organizationPublicId,
                    targetSupplier.getId(),
                    PoStatus.DELETED
            );

            if (!hasRelation) {
                throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
            }

            return SupplierResponse.fromEntity(targetSupplier);
        }

        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);

        if (loginSupplier.getId().equals(targetSupplier.getId())
                || !supplierRelationService.hasVisibleRelation(loginSupplier.getId(), targetSupplier.getId())) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }

        return SupplierResponse.fromEntity(targetSupplier);
    }





    @Transactional(readOnly = true)
    public SupplierResponse getMySupplier(String organizationPublicId, String organizationType) {
        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);
        return SupplierResponse.fromEntity(loginSupplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierListResponse> getSupplierList(
            Pageable pageable,
            SupplierSearchDto searchDto,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (isAdmin(userRole)) {
            // 검색 조건이 있으면 ES 통합검색 실행
            if (hasSearchCondition(searchDto)) {
                return supplierSearchService.search(pageable, searchDto)
                        .map(searchResult -> getSupplierOrThrow(searchResult.getPublicId()))
                        .map(this::toSupplierListResponse);
            }

//        검색 조건이 없으면 db 목록 조회
            return supplierRepository.findAllBySupplierStatusNot(
                            SupplierStatus.TERMINATED,
                            pageable
                    )
                    .map(this::toSupplierListResponse);
        }

        if (BUYER_ORGANIZATION_TYPE.equals(organizationType)) {
            return getBuyerSupplierList(pageable, searchDto, organizationPublicId);
        }

        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);

        if (hasSearchCondition(searchDto)) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_SEARCH_FORBIDDEN);
        }

        SupplierConnectionAggregation aggregation = buildSupplierConnectionAggregation(loginSupplier);

        Map<Long, List<SupplyPurchaseOrder>> purchaseOrdersBySupplierId =
                groupDirectPurchaseOrdersBySupplierId(
                        loginSupplier.getOrganizationPublicId(),
                        loginSupplier.getId()
                );

        Map<Long, SupplySupplier> connectedSupplierMap = new LinkedHashMap<>(aggregation.relatedSupplierMap());

        purchaseOrdersBySupplierId.values().stream()
                .flatMap(List::stream)
                .forEach(po -> connectedSupplierMap.putIfAbsent(po.getSupplier().getId(), po.getSupplier()));

        List<SupplierListResponse> content = connectedSupplierMap.values().stream()
                .map(relatedSupplier -> {
                    List<SupplySubPurchaseOrder> relatedSubOrders =
                            aggregation.ordersByRelatedSupplierId().getOrDefault(relatedSupplier.getId(), List.of());

                    List<Shipment> relatedShipments =
                            aggregation.shipmentsByRelatedSupplierId().getOrDefault(relatedSupplier.getId(), List.of());

                    List<SupplyPurchaseOrder> directPurchaseOrders =
                            purchaseOrdersBySupplierId.getOrDefault(relatedSupplier.getId(), List.of());

                    BigDecimal cumulativeAmount = sumSubOrderAmounts(relatedSubOrders)
                            .add(sumPurchaseOrderAmounts(directPurchaseOrders));

                    SupplySupplierRelation relation = aggregation.relationMap().get(relatedSupplier.getId());
                    SupplierRelationStatus relationStatus =
                            relation != null ? relation.getRelationStatus() : SupplierRelationStatus.ACTIVE;

                    return SupplierListResponse.of(
                            relatedSupplier,
                            calculateOnTimeRate(relatedShipments),
                            null,
                            null,
                            (long) (relatedSubOrders.size() + directPurchaseOrders.size()),
                            cumulativeAmount,
                            cumulativeAmount,
                            relationStatus
                    );
                })
                .toList();

        return toPage(content, pageable);

    }

    public SupplierResponse updateSupplier(String supplierPublicId, String organizationPublicId, UpdateSupplierRequest request) {
        validateOrganizationHeader(organizationPublicId);

        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        validateOwner(supplier, organizationPublicId);

        if (supplierRepository.existsBySupplierCodeAndIdNotAndSupplierStatusNot(
                request.getSupplierCode(),
                supplier.getId(),
                SupplierStatus.TERMINATED
        )) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CODE_ALREADY_EXISTS);
        }

        supplier.update(
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );
        // 수정된 협력사 정보를 ES 문서에도 반영
        supplierSearchService.saveSupplierDocument(supplier);

        return SupplierResponse.fromEntity(supplier);
    }

    public void deleteSupplier(String supplierPublicId, String organizationPublicId) {
        validateOrganizationHeader(organizationPublicId);

        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        validateOwner(supplier, organizationPublicId);

        List<SupplyItem> items = supplyItemRepository.findAllBySupplier_IdAndStatusIn(
                supplier.getId(),
                List.of(Status.ACTIVE, Status.DEACTIVE)
        );

        for (SupplyItem item : items) {
            item.changeActiveYn(Status.DELETE);
        }
        supplier.softDelete();

        // 종료된 협력사는 ES 검색 결과에서 제거
        supplierSearchService.deleteSupplierDocument(supplier.getId());

    }


    // 검색 조건이 하나라도 들어왔는지 확인
    private boolean hasSearchCondition(SupplierSearchDto searchDto) {
        return searchDto != null
                && (
                // 기본 키워드 검색 여부
                (searchDto.getKeyword() != null && !searchDto.getKeyword().isBlank())

                        // 협력사 상태 조건 여부
                        || searchDto.getSupplierStatus() != null

                        // 조직 기준 조건 여부
                        || (searchDto.getOrganizationPublicId() != null
                        && !searchDto.getOrganizationPublicId().isBlank())

                        // 품목 기준 조건 여부
                        || (searchDto.getItemPublicId() != null
                        && !searchDto.getItemPublicId().isBlank())

                        // 월 생산 가능 수량 조건 여부
                        || searchDto.getMinMonthlyCapacity() != null

                        // 현재 공급 가능 수량 조건 여부
                        || searchDto.getMinAvailableQty() != null

                        // MOQ 조건 여부
                        || searchDto.getMaxMoq() != null

                        // 리드타임 조건 여부
                        || searchDto.getMaxLeadTimeDays() != null

                        // 품질 등급 조건 여부
                        || searchDto.getQualityGrade() != null

                        // 인증서 종류 조건 여부
                        || searchDto.getCertificateTypeId() != null

                        // 인증서 상태 조건 여부
                        || searchDto.getCertificateStatus() != null

                        // 유효한 인증서만 조회할지 여부
                        || searchDto.getOnlyValidCertificate() != null

                        // ESG 등급 조건 여부
                        || searchDto.getEsgGrade() != null

                        // ESG 총점 조건 여부
                        || searchDto.getMinTotalScore() != null
        );
    }

    private SupplySupplier getSupplierOrThrow(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndSupplierStatusNot(
                        supplierPublicId,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND));
    }

    private SupplySupplier getLoginSupplier(String organizationPublicId, String organizationType) {
        validateOrganizationHeader(organizationPublicId);

        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }

        SupplySupplier loginSupplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new SupplierException(SupplierErrorCode.LOGIN_SUPPLIER_NOT_FOUND));

        return loginSupplier;
    }


    private void validateOwner(SupplySupplier supplier, String organizationPublicId) {
        if (!supplier.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }
    }

    private boolean canViewAllSuppliers(String organizationType, String userRole) {
        return isAdmin(userRole);
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new SupplierException(SupplierErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private void validateAdminCreate(String userRole) {
        if (!isAdmin(userRole)) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CREATE_FORBIDDEN);
        }
    }

    private SupplierListResponse toSupplierListResponse(SupplySupplier supplier) {
        return SupplierListResponse.of(
                supplier,
                null,
                null,
                null,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null
        );


    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();

        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }

        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    @Transactional(readOnly = true)
    public ConnectedSupplierSummaryResponse getConnectedSupplierSummary(
            String organizationPublicId,
            String organizationType
    ) {
        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);
        SupplierConnectionAggregation aggregation = buildSupplierConnectionAggregation(loginSupplier);

        Map<Long, List<SupplyPurchaseOrder>> purchaseOrdersBySupplierId =
                groupDirectPurchaseOrdersBySupplierId(
                        loginSupplier.getOrganizationPublicId(),
                        loginSupplier.getId()
                );

        Map<Long, SupplySupplier> connectedSupplierMap = new LinkedHashMap<>(aggregation.relatedSupplierMap());

        purchaseOrdersBySupplierId.values().stream()
                .flatMap(List::stream)
                .forEach(po -> connectedSupplierMap.putIfAbsent(po.getSupplier().getId(), po.getSupplier()));

        List<Shipment> shipments = aggregation.shipmentsByRelatedSupplierId().values().stream()
                .flatMap(List::stream)
                .toList();

        List<SupplySubPurchaseOrder> relatedSubOrders = aggregation.ordersByRelatedSupplierId().values().stream()
                .flatMap(List::stream)
                .toList();

        List<SupplyPurchaseOrder> directPurchaseOrders = purchaseOrdersBySupplierId.values().stream()
                .flatMap(List::stream)
                .toList();

        return ConnectedSupplierSummaryResponse.of(
                (long) connectedSupplierMap.size(),
                calculateOnTimeRate(shipments),
                calculateAverageLeadTimeDays(relatedSubOrders, directPurchaseOrders)
        );

    }

    @Transactional(readOnly = true)
    public ConnectedSupplierDetailResponse getConnectedSupplierDetail(
            String supplierPublicId,
            String organizationPublicId,
            String organizationType
    ) {
        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);
        SupplySupplier targetSupplier = getSupplierOrThrow(supplierPublicId);

        boolean hasVisibleRelation =
                supplierRelationService.hasVisibleRelation(loginSupplier.getId(), targetSupplier.getId());

        boolean hasDirectPurchaseOrder =
                hasDirectPurchaseOrderConnection(loginSupplier.getOrganizationPublicId(), targetSupplier.getId());

        if (loginSupplier.getId().equals(targetSupplier.getId())
                || (!hasVisibleRelation && !hasDirectPurchaseOrder)) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }

        List<SupplySubPurchaseOrder> relatedOrders = subPurchaseOrderRepository.findAllBetweenSuppliers(
                loginSupplier.getId(),
                targetSupplier.getId(),
                SubPoStatus.DELETED
        );

        List<SupplyPurchaseOrder> directPurchaseOrders = purchaseOrderRepository
                .findAllByBuyerOrganizationPublicIdAndPoStatusNot(
                        loginSupplier.getOrganizationPublicId(),
                        PoStatus.DELETED
                ).stream()
                .filter(po -> po.getSupplier().getId().equals(targetSupplier.getId()))
                .toList();

        List<Shipment> shipments = relatedOrders.isEmpty()
                ? List.of()
                : shipmentRepository.findAllBySubPoIdIn(
                relatedOrders.stream().map(SupplySubPurchaseOrder::getSubPoId).toList()
        );

        return ConnectedSupplierDetailResponse.of(
                targetSupplier,
                calculateOnTimeRate(shipments),
                (long) (relatedOrders.size() + directPurchaseOrders.size()),
                sumSubOrderAmounts(relatedOrders).add(sumPurchaseOrderAmounts(directPurchaseOrders)),
                relatedOrders.stream()
                        .map(order -> ConnectedSupplierOrderResponse.of(loginSupplier.getId(), order))
                        .toList()
        );
    }

    private SupplierConnectionAggregation buildSupplierConnectionAggregation(SupplySupplier loginSupplier) {
        List<SupplySupplierRelation> relations = supplierRelationService.getVisibleRelations(loginSupplier.getId());

        Map<Long, SupplySupplier> relatedSupplierMap = new LinkedHashMap<>();
        Map<Long, SupplySupplierRelation> relationMap = new LinkedHashMap<>();

        for (SupplySupplierRelation relation : relations) {
            SupplySupplier relatedSupplier = relation.getParentSupplier().getId().equals(loginSupplier.getId())
                    ? relation.getChildSupplier()
                    : relation.getParentSupplier();

            if (!relatedSupplier.getId().equals(loginSupplier.getId())) {
                relatedSupplierMap.putIfAbsent(relatedSupplier.getId(), relatedSupplier);
                relationMap.putIfAbsent(relatedSupplier.getId(), relation);
            }
        }

        if (relatedSupplierMap.isEmpty()) {
            return new SupplierConnectionAggregation(relatedSupplierMap, relationMap, Map.of(), Map.of());
        }

        List<SupplySubPurchaseOrder> relatedOrders =
                subPurchaseOrderRepository.findAllBetweenSupplierAndRelatedSuppliers(
                        loginSupplier.getId(),
                        relatedSupplierMap.keySet(),
                        SubPoStatus.DELETED
                );

        Map<Long, List<SupplySubPurchaseOrder>> ordersByRelatedSupplierId = relatedOrders.stream()
                .collect(Collectors.groupingBy(
                        subPo -> resolveCounterpartSupplierId(loginSupplier.getId(), subPo),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, List<Shipment>> shipmentsByRelatedSupplierId = Map.of();
        if (!relatedOrders.isEmpty()) {
            Map<Long, Long> relatedSupplierIdBySubPoId = relatedOrders.stream()
                    .collect(Collectors.toMap(
                            SupplySubPurchaseOrder::getSubPoId,
                            subPo -> resolveCounterpartSupplierId(loginSupplier.getId(), subPo),
                            (left, right) -> left,
                            LinkedHashMap::new
                    ));

            shipmentsByRelatedSupplierId = shipmentRepository.findAllBySubPoIdIn(relatedSupplierIdBySubPoId.keySet()).stream()
                    .filter(shipment -> shipment.getSubPoId() != null)
                    .filter(shipment -> relatedSupplierIdBySubPoId.containsKey(shipment.getSubPoId()))
                    .collect(Collectors.groupingBy(
                            shipment -> relatedSupplierIdBySubPoId.get(shipment.getSubPoId()),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
        }

        return new SupplierConnectionAggregation(
                relatedSupplierMap,
                relationMap,
                ordersByRelatedSupplierId,
                shipmentsByRelatedSupplierId
        );
    }

    private Long resolveCounterpartSupplierId(Long loginSupplierId, SupplySubPurchaseOrder subPo) {
        if (subPo.getParentPurchaseOrder().getSupplier().getId().equals(loginSupplierId)) {
            return subPo.getSupplier().getId();
        }
        return subPo.getParentPurchaseOrder().getSupplier().getId();
    }

    private BigDecimal calculateOnTimeRate(List<Shipment> shipments) {
        long eligibleShipmentCount = shipments.stream()
                .filter(shipment -> shipment.getArrivalEta() != null && shipment.getActualArrivedAt() != null)
                .count();

        if (eligibleShipmentCount == 0) {
            return null;
        }

        long onTimeShipmentCount = shipments.stream()
                .filter(shipment -> shipment.getArrivalEta() != null && shipment.getActualArrivedAt() != null)
                .filter(shipment -> !shipment.getActualArrivedAt().isAfter(shipment.getArrivalEta()))
                .count();

        return BigDecimal.valueOf(onTimeShipmentCount * 100.0)
                .divide(BigDecimal.valueOf(eligibleShipmentCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumSubOrderAmounts(List<SupplySubPurchaseOrder> orders) {
        return orders.stream()
                .map(SupplySubPurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record SupplierConnectionAggregation(
            Map<Long, SupplySupplier> relatedSupplierMap,
            Map<Long, SupplySupplierRelation> relationMap,
            Map<Long, List<SupplySubPurchaseOrder>> ordersByRelatedSupplierId,
            Map<Long, List<Shipment>> shipmentsByRelatedSupplierId
    ) {}

    private Integer calculateAverageLeadTimeDays(List<SupplySubPurchaseOrder> orders) {
        double averageLeadTime = orders.stream()
                .flatMap(order -> order.getActiveItems().stream())
                .map(SupplySubPurchaseOrderItem::getLeadTimeDays)
                .filter(leadTimeDays -> leadTimeDays != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageLeadTime);
    }

    private Page<SupplierListResponse> getBuyerSupplierList(
            Pageable pageable,
            SupplierSearchDto searchDto,
            String buyerOrganizationPublicId
    ) {
        validateOrganizationHeader(buyerOrganizationPublicId);

        Map<Long, BuyerSupplierAggregate> aggregateMap = new LinkedHashMap<>();

        for (SupplyPurchaseOrder purchaseOrder : purchaseOrderRepository.findAllByBuyerOrganizationPublicIdAndPoStatusNot(
                buyerOrganizationPublicId,
                PoStatus.DELETED
        )) {
            SupplySupplier supplier = purchaseOrder.getSupplier();

            if (supplier.getSupplierStatus() == SupplierStatus.TERMINATED) {
                continue;
            }

            if (!matchesBuyerSupplierSearch(supplier, searchDto)) {
                continue;
            }

            aggregateMap.compute(
                    supplier.getId(),
                    (supplierId, existing) -> existing == null
                            ? new BuyerSupplierAggregate(
                            supplier,
                            1L,
                            purchaseOrder.getTotalAmount() != null ? purchaseOrder.getTotalAmount() : BigDecimal.ZERO
                    )
                            : existing.add(purchaseOrder.getTotalAmount())
            );
        }

        List<SupplierListResponse> content = aggregateMap.values().stream()
                .sorted(Comparator.comparing(aggregate -> aggregate.supplier().getSupplierName()))
                .map(aggregate -> SupplierListResponse.of(
                        aggregate.supplier(),
                        null,
                        null,
                        null,
                        aggregate.purchaseOrderCount(),
                        aggregate.cumulativeAmount(),
                        aggregate.cumulativeAmount(),
                        SupplierRelationStatus.ACTIVE
                ))
                .toList();

        return toPage(content, pageable);
    }

    private boolean matchesBuyerSupplierSearch(SupplySupplier supplier, SupplierSearchDto searchDto) {
        if (searchDto == null) {
            return true;
        }

        if (searchDto.getSupplierStatus() != null
                && supplier.getSupplierStatus() != searchDto.getSupplierStatus()) {
            return false;
        }

        String keyword = searchDto.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        return containsIgnoreCase(supplier.getSupplierCode(), normalizedKeyword)
                || containsIgnoreCase(supplier.getSupplierName(), normalizedKeyword)
                || containsIgnoreCase(supplier.getPrimaryContactName(), normalizedKeyword)
                || containsIgnoreCase(supplier.getPrimaryContactEmail(), normalizedKeyword)
                || containsIgnoreCase(supplier.getPrimaryContactPhone(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword);
    }

    private record BuyerSupplierAggregate(
            SupplySupplier supplier,
            Long purchaseOrderCount,
            BigDecimal cumulativeAmount
    ) {
        private BuyerSupplierAggregate add(BigDecimal amount) {
            return new BuyerSupplierAggregate(
                    supplier,
                    purchaseOrderCount + 1,
                    cumulativeAmount.add(amount != null ? amount : BigDecimal.ZERO)
            );
        }
    }

    private Map<Long, List<SupplyPurchaseOrder>> groupDirectPurchaseOrdersBySupplierId(
            String buyerOrganizationPublicId,
            Long loginSupplierId
    ) {
        return purchaseOrderRepository.findAllByBuyerOrganizationPublicIdAndPoStatusNot(
                        buyerOrganizationPublicId,
                        PoStatus.DELETED
                ).stream()
                .filter(po -> !po.getSupplier().getId().equals(loginSupplierId))
                .collect(Collectors.groupingBy(
                        po -> po.getSupplier().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private BigDecimal sumPurchaseOrderAmounts(List<SupplyPurchaseOrder> orders) {
        return orders.stream()
                .map(SupplyPurchaseOrder::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasDirectPurchaseOrderConnection(
            String buyerOrganizationPublicId,
            Long targetSupplierId
    ) {
        return purchaseOrderRepository.existsByBuyerOrganizationPublicIdAndSupplier_IdAndPoStatusNot(
                buyerOrganizationPublicId,
                targetSupplierId,
                PoStatus.DELETED
        );
    }

    private Integer calculateAverageLeadTimeDays(
            List<SupplySubPurchaseOrder> subOrders,
            List<SupplyPurchaseOrder> purchaseOrders
    ) {
        double averageLeadTime = java.util.stream.Stream.concat(
                        subOrders.stream()
                                .flatMap(order -> order.getActiveItems().stream())
                                .map(SupplySubPurchaseOrderItem::getLeadTimeDays),
                        purchaseOrders.stream()
                                .flatMap(order -> order.getActiveItems().stream())
                                .map(SupplyPurchaseOrderItem::getLeadTimeDays)
                )
                .filter(leadTimeDays -> leadTimeDays != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageLeadTime);
    }














}
