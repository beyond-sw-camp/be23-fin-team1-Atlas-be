package com.ozz.atlas.supply.settlement.service;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnItem;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.settlement.search.service.SettlementSearchService;
import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementDetail;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import com.ozz.atlas.supply.settlement.dtos.CreateSettlementRequestDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementDetailResponseDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.exception.SettlementErrorCode;
import com.ozz.atlas.supply.settlement.exception.SettlementException;
import com.ozz.atlas.supply.settlement.repository.SettlementDetailRepository;
import com.ozz.atlas.supply.settlement.repository.SettlementRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ShipmentRepository shipmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final SettlementSearchService settlementSearchService;

    @Transactional(readOnly = true)
    public String getSettlementPublicIdByTargetPublicId(String targetPublicId, SettlementTargetType targetType) {
        return settlementRepository.findByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                targetType,
                targetPublicId,
                SettlementStatus.CANCELLED
        ).map(Settlement::getPublicId).orElse(null);
    }

    // 정산 생성 -> 상세 금액 합계를 헤더 amount에 반영
    @Transactional
    public SettlementResponseDto createSettlement(
            CreateSettlementRequestDto request,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);
        validateCreateRequest(request);

        SettlementContext context = resolveSettlementContext(request, actorOrganizationPublicId);

        Settlement settlement = Settlement.builder()
                .buyerOrganizationPublicId(context.buyerOrganizationPublicId())
                .supplierOrganizationPublicId(context.supplierOrganizationPublicId())
                .supplierId(context.supplier().getId())
                .targetType(request.getTargetType())
                .targetPublicId(request.getTargetPublicId())
                .settlementPeriodStart(request.getSettlementPeriodStart())
                .settlementPeriodEnd(request.getSettlementPeriodEnd())
                .currencyCode(request.getCurrencyCode())
                .amount(BigDecimal.ZERO)
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        List<SettlementDetail> details = request.getTargetType() == SettlementTargetType.RETURN
                ? createReturnSettlementDetails(savedSettlement, request)
                : createShipmentSettlementDetails(savedSettlement, request.getTargetPublicId());

        List<SettlementDetail> savedDetails = settlementDetailRepository.saveAll(details);

        BigDecimal totalAmount = savedDetails.stream()
                .map(SettlementDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedSettlement.updateAmount(totalAmount);

        settlementSearchService.saveSettlementDocument(savedSettlement);

        return toResponseDto(savedSettlement, context.supplier().getPublicId(), savedDetails);
    }

    @Transactional
    public void createShipmentSettlementIfAbsent(String shipmentPublicId) {
        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.SHIPMENT,
                shipmentPublicId,
                SettlementStatus.CANCELLED
        )) {
            return;
        }

        Shipment shipment = getShipmentByPublicId(shipmentPublicId);
        LogisticsNode destinationNode = getLogisticsNodeById(shipment.getDestinationNodeId());

        createSettlement(
                CreateSettlementRequestDto.builder()
                        .targetType(SettlementTargetType.SHIPMENT)
                        .targetPublicId(shipmentPublicId)
                        .currencyCode(resolveShipmentCurrencyCode(shipment))
                        .build(),
                destinationNode.getOrganizationPublicId(),
                "SYSTEM"
        );
    }

    @Transactional
    public void createReturnSettlementIfAbsent(String returnPublicId) {
        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.RETURN,
                returnPublicId,
                SettlementStatus.CANCELLED
        )) {
            return;
        }

        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(returnPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
            // EXCHANGE는 상품 대금 차감 정산이 발생하지 않음. 물류비 정산은 별도 로직 또는 수기 처리 필요.
            return;
        }

        createSettlement(
                CreateSettlementRequestDto.builder()
                        .targetType(SettlementTargetType.RETURN)
                        .targetPublicId(returnPublicId)
                        .currencyCode(resolveReturnCurrencyCode(returnRequest))
                        .build(),
                returnRequest.getRequestOrganizationPublicId(),
                "SYSTEM"
        );
    }

    // 정산 목록 조회
    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> getSettlements(
            Pageable pageable,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        return settlementRepository.findReadableByOrganizationPublicId(actorOrganizationPublicId, pageable)
                .map(this::toResponseDtoWithoutDetails);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> searchSettlements(
            Pageable pageable,
            com.ozz.atlas.supply.settlement.search.dtos.SettlementSearchDto searchDto,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        return settlementSearchService.search(pageable, searchDto, actorOrganizationPublicId);
    }

    // 정산 상세 조회
    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 승인 -> 연결된 모든 상세 항목 승인 상태로 전환
    @Transactional
    public SettlementResponseDto approveSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String approvedByUserPublicId,
            String userRole
    ) {
        validateSettlementActionHeader(actorOrganizationPublicId, approvedByUserPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        if (!actorOrganizationPublicId.equals(settlement.getSupplierOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_APPROVAL);
        }

        try {
            settlement.approve(approvedByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::approve);

        settlementSearchService.saveSettlementDocument(settlement);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 취소 -> 연결된 모든 상세 항목 취소 상태로 전환
    @Transactional
    public SettlementResponseDto cancelSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String cancelledByUserPublicId,
            String userRole
    ) {
        validateSettlementActionHeader(actorOrganizationPublicId, cancelledByUserPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        if (!actorOrganizationPublicId.equals(settlement.getBuyerOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CANCEL);
        }

        try {
            settlement.cancel(cancelledByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::cancel);

        settlementSearchService.saveSettlementDocument(settlement);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 기존 정산 조회/응답 변환 시 저장된 supplierId 기준으로 협력사 조회
    private String getSupplierPublicId(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .map(SupplySupplier::getPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));
    }

    // 정산 헤더와 상세 목록을 포함한 상세 응답 DTO 변환
    private SettlementResponseDto toResponseDto(
            Settlement settlement,
            String supplierPublicId,
            List<SettlementDetail> details
    ) {
        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .publicId(settlement.getPublicId())
                .buyerOrganizationPublicId(settlement.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(settlement.getSupplierOrganizationPublicId())
                .supplierPublicId(supplierPublicId)
                .targetType(settlement.getTargetType())
                .targetPublicId(settlement.getTargetPublicId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .amount(settlement.getAmount())
                .currencyCode(settlement.getCurrencyCode())
                .settlementStatus(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .approvedByUserPublicId(settlement.getApprovedByUserPublicId())
                .cancelledAt(settlement.getCancelledAt())
                .cancelledByUserPublicId(settlement.getCancelledByUserPublicId())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .details(details.stream()
                        .map(this::toDetailResponseDto)
                        .toList())
                .build();
    }

    // 정산 목록 조회용 응답 DTO 변환
    private SettlementResponseDto toResponseDtoWithoutDetails(Settlement settlement) {
        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .publicId(settlement.getPublicId())
                .buyerOrganizationPublicId(settlement.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(settlement.getSupplierOrganizationPublicId())
                .supplierPublicId(supplierPublicId)
                .targetType(settlement.getTargetType())
                .targetPublicId(settlement.getTargetPublicId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .amount(settlement.getAmount())
                .currencyCode(settlement.getCurrencyCode())
                .settlementStatus(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .approvedByUserPublicId(settlement.getApprovedByUserPublicId())
                .cancelledAt(settlement.getCancelledAt())
                .cancelledByUserPublicId(settlement.getCancelledByUserPublicId())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .details(List.of())
                .build();
    }

    // 정산 상세 엔티티를 응답 DTO 변환
    private SettlementDetailResponseDto toDetailResponseDto(SettlementDetail detail) {
        return SettlementDetailResponseDto.builder()
                .publicId(detail.getPublicId())
                .poItemId(detail.getPoItemId())
                .itemId(detail.getItemId())
                .qty(detail.getQty())
                .unitPrice(detail.getUnitPrice())
                .amount(detail.getAmount())
                .detailStatus(detail.getDetailStatus())
                .build();
    }

    private static final String ADMIN_ROLE = "ADMIN";

//    반품-정산 요청 검증
    private void validateCreateRequest(CreateSettlementRequestDto request) {
        if (request.getTargetType() != SettlementTargetType.SHIPMENT
                && request.getTargetType() != SettlementTargetType.RETURN) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                request.getTargetType(),
                request.getTargetPublicId(),
                SettlementStatus.CANCELLED
        )) {
            throw new SettlementException(SettlementErrorCode.DUPLICATE_SETTLEMENT_TARGET);
        }
    }

    private SettlementContext resolveSettlementContext(
            CreateSettlementRequestDto request,
            String actorOrganizationPublicId
    ) {
        if (request.getTargetType() == SettlementTargetType.SHIPMENT) {
            return resolveShipmentSettlementContext(request.getTargetPublicId(), actorOrganizationPublicId);
        }

        if (request.getTargetType() == SettlementTargetType.RETURN) {
            return resolveReturnSettlementContext(request.getTargetPublicId(), actorOrganizationPublicId);
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private SettlementContext resolveShipmentSettlementContext(
            String shipmentPublicId,
            String actorOrganizationPublicId
    ) {
        Shipment shipment = getShipmentByPublicId(shipmentPublicId);

        if (shipment.getStatus() != ShipmentStatus.ARRIVED) {
            throw new SettlementException(SettlementErrorCode.SHIPMENT_NOT_SETTLABLE);
        }

        LogisticsNode originNode = getLogisticsNodeById(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getLogisticsNodeById(shipment.getDestinationNodeId());

        String supplierOrganizationPublicId = originNode.getOrganizationPublicId();
        String buyerOrganizationPublicId = destinationNode.getOrganizationPublicId();

        if (!actorOrganizationPublicId.equals(buyerOrganizationPublicId)) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CREATE);
        }

        SupplySupplier supplier = getSettlementSupplierByOrganizationPublicId(supplierOrganizationPublicId);

        return new SettlementContext(
                supplier,
                buyerOrganizationPublicId,
                supplierOrganizationPublicId
        );
    }

    private SettlementContext resolveReturnSettlementContext(
            String returnPublicId,
            String actorOrganizationPublicId
    ) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(returnPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getReturnStatus() != ReturnStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.RETURN_NOT_SETTLABLE);
        }

        if (!actorOrganizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CREATE);
        }

        SupplySupplier supplier = getSettlementSupplierByOrganizationPublicId(
                returnRequest.getTargetOrganizationPublicId()
        );

        return new SettlementContext(
                supplier,
                returnRequest.getRequestOrganizationPublicId(),
                returnRequest.getTargetOrganizationPublicId()
        );
    }

    private List<SettlementDetail> createShipmentSettlementDetails(
            Settlement settlement,
            String shipmentPublicId
    ) {
        Shipment shipment = getShipmentByPublicId(shipmentPublicId);

        if (shipment.getPoId() != null) {
            return createPurchaseOrderSettlementDetails(settlement, shipment.getPoId());
        }

        if (shipment.getSubPoId() != null) {
            return createSubPurchaseOrderSettlementDetails(settlement, shipment.getSubPoId());
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private List<SettlementDetail> createPurchaseOrderSettlementDetails(
            Settlement settlement,
            Long poId
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        List<SettlementDetail> details = purchaseOrder.getActiveItems().stream()
                .map(item -> toPurchaseOrderSettlementDetail(settlement, item))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private List<SettlementDetail> createSubPurchaseOrderSettlementDetails(
            Settlement settlement,
            Long subPoId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(subPoId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        List<SettlementDetail> details = subPurchaseOrder.getActiveItems().stream()
                .map(item -> toSubPurchaseOrderSettlementDetail(settlement, item))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private SettlementDetail toPurchaseOrderSettlementDetail(
            Settlement settlement,
            SupplyPurchaseOrderItem item
    ) {
        BigDecimal qty = toSettlementQty(item.getConfirmedQty(), item.getOrderedQty());
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(item.getPoItemId())
                .itemId(item.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private SettlementDetail toSubPurchaseOrderSettlementDetail(
            Settlement settlement,
            SupplySubPurchaseOrderItem item
    ) {
        BigDecimal qty = toSettlementQty(item.getConfirmedQty(), item.getOrderedQty());
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(item.getParentPurchaseOrderItem().getPoItemId())
                .itemId(item.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private BigDecimal toSettlementQty(Long confirmedQty, Long orderedQty) {
        Long qty = confirmedQty != null ? confirmedQty : orderedQty;

        if (qty == null || qty <= 0) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return BigDecimal.valueOf(qty);
    }

    private Shipment getShipmentByPublicId(String shipmentPublicId) {
        return shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SHIPMENT_NOT_FOUND));
    }

    private SettlementCurrency resolveShipmentCurrencyCode(Shipment shipment) {
        if (shipment.getPoId() != null) {
            SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(shipment.getPoId())
                    .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));
            return toSettlementCurrency(purchaseOrder.getCurrencyCode().name());
        }

        if (shipment.getSubPoId() != null) {
            return SettlementCurrency.KRW;
        }

        return SettlementCurrency.KRW;
    }

    private SettlementCurrency resolveReturnCurrencyCode(ReturnRequest returnRequest) {
        Shipment shipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        return resolveShipmentCurrencyCode(shipment);
    }

    private SettlementCurrency toSettlementCurrency(String currencyCode) {
        if ("USD".equalsIgnoreCase(currencyCode) || "DOLLAR".equalsIgnoreCase(currencyCode)) {
            return SettlementCurrency.DOLLAR;
        }

        return SettlementCurrency.KRW;
    }

    private LogisticsNode getLogisticsNodeById(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));
    }

    private SupplySupplier getSettlementSupplierByOrganizationPublicId(String organizationPublicId) {
        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));

        if (supplier.getSupplierStatus() == SupplierStatus.TERMINATED) {
            throw new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND);
        }

        return supplier;
    }

    private void validateSettlementActorHeader(String actorOrganizationPublicId, String userRole) {
        if (actorOrganizationPublicId == null || actorOrganizationPublicId.isBlank()
                || userRole == null || userRole.isBlank()) {
            throw new SettlementException(SettlementErrorCode.INVALID_ACTOR_HEADER);
        }

        if (ADMIN_ROLE.equals(userRole)) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_ACCESS);
        }
    }

    private void validateSettlementActionHeader(
            String actorOrganizationPublicId,
            String actorUserPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        if (actorUserPublicId == null || actorUserPublicId.isBlank()) {
            throw new SettlementException(SettlementErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private record SettlementContext(
            SupplySupplier supplier,
            String buyerOrganizationPublicId,
            String supplierOrganizationPublicId
    ) {
    }

    //    반품 정산 상세 생성
    private List<SettlementDetail> createReturnSettlementDetails(
            Settlement settlement,
            CreateSettlementRequestDto request
    ) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(request.getTargetPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getReturnStatus() != ReturnStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.RETURN_NOT_SETTLABLE);
        }

        Shipment shipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (shipment.getPoId() != null) {
            return createPurchaseOrderReturnSettlementDetails(settlement, shipment.getPoId(), returnRequest);
        }

        if (shipment.getSubPoId() != null) {
            return createSubPurchaseOrderReturnSettlementDetails(settlement, shipment.getSubPoId(), returnRequest);
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private List<SettlementDetail> createPurchaseOrderReturnSettlementDetails(
            Settlement settlement,
            Long poId,
            ReturnRequest returnRequest
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!purchaseOrder.getSupplier().getId().equals(settlement.getSupplierId())) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        List<SettlementDetail> details = returnRequest.getItems().stream()
                .map(returnItem -> toPurchaseOrderReturnSettlementDetail(settlement, purchaseOrder, returnItem))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private List<SettlementDetail> createSubPurchaseOrderReturnSettlementDetails(
            Settlement settlement,
            Long subPoId,
            ReturnRequest returnRequest
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(subPoId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!subPurchaseOrder.getSupplier().getId().equals(settlement.getSupplierId())) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        List<SettlementDetail> details = returnRequest.getItems().stream()
                .map(returnItem -> toSubPurchaseOrderReturnSettlementDetail(settlement, subPurchaseOrder, returnItem))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    //    반품 item 1건을 정산 detail로 바꾸는 메서드
    private SettlementDetail toPurchaseOrderReturnSettlementDetail(
            Settlement settlement,
            SupplyPurchaseOrder purchaseOrder,
            ReturnItem returnItem
    ) {
        SupplyPurchaseOrderItem poItem = purchaseOrder.getPurchaseOrderItems().stream()
                .filter(item -> item.getItem().getPublicId().equals(returnItem.getItemPublicId()))
                .findFirst()
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        BigDecimal qty = returnItem.getReturnQty().negate();
        BigDecimal unitPrice = poItem.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(poItem.getPoItemId())
                .itemId(poItem.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private SettlementDetail toSubPurchaseOrderReturnSettlementDetail(
            Settlement settlement,
            SupplySubPurchaseOrder subPurchaseOrder,
            ReturnItem returnItem
    ) {
        SupplySubPurchaseOrderItem subPoItem = subPurchaseOrder.getSubPurchaseOrderItems().stream()
                .filter(item -> item.getItem().getPublicId().equals(returnItem.getItemPublicId()))
                .findFirst()
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        BigDecimal qty = returnItem.getReturnQty().negate();
        BigDecimal unitPrice = subPoItem.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(subPoItem.getParentPurchaseOrderItem().getPoItemId())
                .itemId(subPoItem.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

}
