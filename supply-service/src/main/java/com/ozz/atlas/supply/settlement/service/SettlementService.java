package com.ozz.atlas.supply.settlement.service;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnItem;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementDetail;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import com.ozz.atlas.supply.settlement.dtos.CreateSettlementDetailRequestDto;
import com.ozz.atlas.supply.settlement.dtos.CreateSettlementRequestDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementDetailResponseDto;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.exception.SettlementErrorCode;
import com.ozz.atlas.supply.settlement.exception.SettlementException;
import com.ozz.atlas.supply.settlement.repository.SettlementDetailRepository;
import com.ozz.atlas.supply.settlement.repository.SettlementRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 정산 생성 -> 상세 금액 합계를 헤더 amount에 반영
    @Transactional
    public SettlementResponseDto createSettlement(CreateSettlementRequestDto request) {
        SupplySupplier supplier = getApprovedActiveSupplier(request.getSupplierPublicId());

        validateCreateRequest(request);

        Settlement settlement = Settlement.builder()
                .supplierId(supplier.getId())
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
                : createManualSettlementDetails(savedSettlement, request);

        List<SettlementDetail> savedDetails = settlementDetailRepository.saveAll(details);

        BigDecimal totalAmount = savedDetails.stream()
                .map(SettlementDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedSettlement.updateAmount(totalAmount);

        return toResponseDto(savedSettlement, supplier.getPublicId(), savedDetails);
    }


    // 정산 목록 조회
    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> getSettlements(Pageable pageable) {
        return settlementRepository.findAll(pageable)
                .map(this::toResponseDtoWithoutDetails);
    }


    // 정산 상세 조회
    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlement(Long settlementId) {
        Settlement settlement = getSettlementEntity(settlementId);
        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 승인 -> 연결된 모든 상세 항목 승인 상태로 전환
    @Transactional
    public SettlementResponseDto approveSettlement(Long settlementId, String approvedByUserPublicId, String userRole) {
        validateAdminActor(
                approvedByUserPublicId,
                userRole,
                SettlementErrorCode.FORBIDDEN_SETTLEMENT_APPROVAL
        );

        Settlement settlement = getSettlementEntity(settlementId);

        try {
            settlement.approve(approvedByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::approve);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 취소 -> 연결된 모든 상세 항목 취소 상태로 전환
    @Transactional
    public SettlementResponseDto cancelSettlement(Long settlementId, String cancelledByUserPublicId, String userRole) {
        validateAdminActor(
                cancelledByUserPublicId,
                userRole,
                SettlementErrorCode.FORBIDDEN_SETTLEMENT_CANCEL
        );

        Settlement settlement = getSettlementEntity(settlementId);

        try {
            settlement.cancel(cancelledByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::cancel);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 승인 완료되고 종료되지 않은 협력사만 정산 생성 대상으로 허용
    private SupplySupplier getApprovedActiveSupplier(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));
    }

    // 기존 정산 조회/응답 변환 시 저장된 supplierId 기준으로 협력사 조회
    private String getSupplierPublicId(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .map(SupplySupplier::getPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));
    }

    // 정산 id로 헤더 엔티티 조회
    private Settlement getSettlementEntity(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));
    }

    // 정산 상세 생성 요청 DTO를 SettlementDetail 엔티티로 변환
    private SettlementDetail toSettlementDetail(
            Settlement settlement,
            CreateSettlementDetailRequestDto request
    ) {
        BigDecimal qty = request.getQty() != null ? request.getQty() : BigDecimal.ZERO;
        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(request.getPoItemId())
                .itemId(request.getItemId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    // 정산 헤더와 상세 목록을 포함한 상세 응답 DTO 변환
    private SettlementResponseDto toResponseDto(
            Settlement settlement,
            String supplierPublicId,
            List<SettlementDetail> details
    ) {
        return SettlementResponseDto.builder()
                .id(settlement.getId())
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

    //    정산 승인/취소 권한 체크
    private void validateAdminActor(String actorPublicId, String userRole, SettlementErrorCode forbiddenErrorCode) {
        if (actorPublicId == null || actorPublicId.isBlank()
                || userRole == null || userRole.isBlank()) {
            throw new SettlementException(SettlementErrorCode.INVALID_ACTOR_HEADER);
        }

        if (!ADMIN_ROLE.equals(userRole)) {
            throw new SettlementException(forbiddenErrorCode);
        }
    }

//    반품-정산 요청 검증
    private void validateCreateRequest(CreateSettlementRequestDto request) {
        if (request.getTargetType() != SettlementTargetType.RETURN) {
            if (request.getDetails() == null || request.getDetails().isEmpty()) {
                throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
            }
            return;
        }

        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.RETURN,
                request.getTargetPublicId(),
                SettlementStatus.CANCELLED
        )) {
            throw new SettlementException(SettlementErrorCode.DUPLICATE_SETTLEMENT_TARGET);
        }
    }

//    기존 수동 정산 상세 생성 메서드 분리
    private List<SettlementDetail> createManualSettlementDetails(
            Settlement settlement,
            CreateSettlementRequestDto request
    ) {
        return request.getDetails().stream()
                .map(detailRequest -> toSettlementDetail(settlement, detailRequest))
                .toList();
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

        if (shipment.getPoId() == null) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(shipment.getPoId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!purchaseOrder.getSupplier().getId().equals(settlement.getSupplierId())) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return returnRequest.getItems().stream()
                .map(returnItem -> toReturnSettlementDetail(settlement, purchaseOrder, returnItem))
                .toList();
    }


    //    반품 item 1건을 정산 detail로 바꾸는 메서드
    private SettlementDetail toReturnSettlementDetail(
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

}
