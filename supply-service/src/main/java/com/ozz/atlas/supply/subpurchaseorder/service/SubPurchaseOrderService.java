package com.ozz.atlas.supply.subpurchaseorder.service;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import com.ozz.atlas.supply.subpurchaseorder.dtos.ConfirmSubPurchaseOrderItemRequest;
import com.ozz.atlas.supply.subpurchaseorder.dtos.CreateSubPurchaseOrderItemRequest;
import com.ozz.atlas.supply.subpurchaseorder.dtos.CreateSubPurchaseOrderRequest;
import com.ozz.atlas.supply.subpurchaseorder.dtos.SubPurchaseOrderResponse;
import com.ozz.atlas.supply.subpurchaseorder.exception.SubPurchaseOrderErrorCode;
import com.ozz.atlas.supply.subpurchaseorder.exception.SubPurchaseOrderException;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderItemRepository;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubPurchaseOrderService {

    private static final List<SubPurchaseOrderLineStatus> ACTIVE_LINE_STATUSES =
            List.of(
                    SubPurchaseOrderLineStatus.OPEN,
                    SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED,
                    SubPurchaseOrderLineStatus.CONFIRMED
            );

    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SubPurchaseOrderItemRepository subPurchaseOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierItemCapabilityRepository capabilityRepository;

    public SubPurchaseOrderResponse createSubPurchaseOrder(
            String issuerOrganizationPublicId,
            String createdByUserPublicId,
            CreateSubPurchaseOrderRequest request
    ) {
        validateCreateRequest(request);

        SupplyPurchaseOrder parentPurchaseOrder = purchaseOrderRepository
                .findByPublicIdAndSupplier_OrganizationPublicIdAndPoStatusNot(
                        request.getParentPoPublicId(),
                        issuerOrganizationPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.PARENT_PURCHASE_ORDER_NOT_FOUND));

        if (!parentPurchaseOrder.isSubOrderCreatable()) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_STATUS_NOT_ALLOWED);
        }

        // 부모 발주 엔티티를 이미 찾았으므로 동일 문서 번호 중복 검사는 내부 PK 기준으로 처리한다.
        if (subPurchaseOrderRepository.existsBySubPoNumberAndParentPurchaseOrder_IdAndSubPoStatusNot(
                request.getSubPoNumber(),
                parentPurchaseOrder.getId(),
                SubPoStatus.DELETED
        )) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_NUMBER_ALREADY_EXISTS);
        }

        SupplySupplier targetSupplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        request.getSupplierPublicId(),
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.TARGET_SUPPLIER_NOT_FOUND));

        // 둘 다 supply-service 내부 엔티티이므로 동일 협력사 비교는 id가 맞다.
        if (targetSupplier.getId().equals(parentPurchaseOrder.getSupplier().getId())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_SAME_SUPPLIER_NOT_ALLOWED);
        }

        // 요청은 parentPoItemPublicId를 주므로 여기까지는 외부 식별자 해석 단계다.
        Map<String, SupplyPurchaseOrderItem> parentItems = parentPurchaseOrder.getActiveItems().stream()
                .collect(Collectors.toMap(SupplyPurchaseOrderItem::getPublicId, Function.identity()));

        List<SupplySubPurchaseOrderItem> items = request.getItems().stream()
                .map(itemRequest -> createSubPurchaseOrderItem(
                        parentItems,
                        targetSupplier,
                        request.getDueDate(),
                        itemRequest
                ))
                .toList();

        SupplySubPurchaseOrder subPurchaseOrder = SupplySubPurchaseOrder.create(
                request.getSubPoNumber(),
                parentPurchaseOrder,
                targetSupplier,
                request.getDueDate(),
                createdByUserPublicId,
                items
        );

        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrderRepository.save(subPurchaseOrder), true);
    }

    @Transactional(readOnly = true)
    public Page<SubPurchaseOrderResponse> getSubPurchaseOrdersByParentPo(
            String issuerOrganizationPublicId,
            String parentPoPublicId,
            Pageable pageable
    ) {
        return subPurchaseOrderRepository
                .findAllByParentPurchaseOrder_PublicIdAndParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
                        parentPoPublicId,
                        issuerOrganizationPublicId,
                        SubPoStatus.DELETED,
                        pageable
                )
                .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
    }

    @Transactional(readOnly = true)
    public Page<SubPurchaseOrderResponse> getReceivedSubPurchaseOrders(
            String receiverOrganizationPublicId,
            Pageable pageable
    ) {
        return subPurchaseOrderRepository
                .findAllBySupplier_OrganizationPublicIdAndSubPoStatusNot(
                        receiverOrganizationPublicId,
                        SubPoStatus.DELETED,
                        pageable
                )
                .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
    }

    @Transactional(readOnly = true)
    public SubPurchaseOrderResponse getSubPurchaseOrder(
            String organizationPublicId,
            String subPoPublicId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository
                .findByPublicIdAndParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
                        subPoPublicId,
                        organizationPublicId,
                        SubPoStatus.DELETED
                )
                .orElse(
                        subPurchaseOrderRepository
                                .findByPublicIdAndSupplier_OrganizationPublicIdAndSubPoStatusNot(
                                        subPoPublicId,
                                        organizationPublicId,
                                        SubPoStatus.DELETED
                                )
                                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ACCESS_DENIED))
                );

        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    public SubPurchaseOrderResponse acceptSubPurchaseOrder(
            String receiverOrganizationPublicId,
            String subPoPublicId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverActionable(subPurchaseOrder);

        subPurchaseOrder.accept();
        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    public SubPurchaseOrderResponse rejectSubPurchaseOrder(
            String receiverOrganizationPublicId,
            String subPoPublicId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverActionable(subPurchaseOrder);

        subPurchaseOrder.reject();
        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    public SubPurchaseOrderResponse confirmSubPurchaseOrderItem(
            String receiverOrganizationPublicId,
            String subPoPublicId,
            String poItemPublicId,
            ConfirmSubPurchaseOrderItemRequest request
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverConfirmable(subPurchaseOrder);

        // 외부 path는 parentPoItemPublicId로 받지만, 상세 라인을 찾은 뒤부터는 내부 PK 기반 검증으로 간다.
        SupplySubPurchaseOrderItem item = subPurchaseOrder.findActiveItemByParentPoItemPublicId(poItemPublicId);
        if (item == null) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ITEM_NOT_FOUND);
        }

        if (request.getConfirmedQty().compareTo(item.getOrderedQty()) > 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_CONFIRM_QTY_INVALID);
        }

        item.confirm(request.getConfirmedQty());
        subPurchaseOrder.refreshConfirmationStatus();

        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    private void validateCreateRequest(CreateSubPurchaseOrderRequest request) {
        long distinctCount = request.getItems().stream()
                .map(CreateSubPurchaseOrderItemRequest::getParentPoItemPublicId)
                .distinct()
                .count();

        if (distinctCount != request.getItems().size()) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_DUPLICATE_PARENT_ITEM);
        }
    }

    private SupplySubPurchaseOrderItem createSubPurchaseOrderItem(
            Map<String, SupplyPurchaseOrderItem> parentItems,
            SupplySupplier targetSupplier,
            LocalDate subPoDueDate,
            CreateSubPurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrderItem parentItem = parentItems.get(request.getParentPoItemPublicId());
        if (parentItem == null) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ITEM_NOT_FOUND);
        }

        if (!parentItem.isSubOrderable()) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_STATUS_NOT_ALLOWED);
        }

        BigDecimal alreadyAllocatedQty =
                subPurchaseOrderItemRepository.sumOrderedQtyByParentPurchaseOrderItemIdAndLineStatusIn(
                        parentItem.getPoItemId(),
                        ACTIVE_LINE_STATUSES
                );

        BigDecimal requestedQty = request.getOrderedQty();
        BigDecimal baseQty = parentItem.getSubOrderBaseQty();

        if (alreadyAllocatedQty.add(requestedQty).compareTo(baseQty) > 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_QTY_EXCEEDED);
        }

        LocalDate requiredDate = request.getRequiredDate() != null
                ? request.getRequiredDate()
                : parentItem.getRequiredDate();

        if (requiredDate.isAfter(subPoDueDate) || requiredDate.isAfter(parentItem.getRequiredDate())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        validateSupplierCapability(targetSupplier.getId(), parentItem, requestedQty, requiredDate);

        return SupplySubPurchaseOrderItem.create(
                parentItem,
                requestedQty,
                requiredDate
        );
    }

    private void validateSupplierCapability(
            Long targetSupplierId,
            SupplyPurchaseOrderItem parentItem,
            BigDecimal requestedQty,
            LocalDate requiredDate
    ) {
        Long itemId = parentItem.getItem().getId();

        SupplySupplierItemCapability capability = capabilityRepository
                .findBySupplier_IdAndItem_Id(targetSupplierId, itemId)
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.TARGET_SUPPLIER_CAPABILITY_NOT_FOUND));

        if (capability.getValidFrom() != null && requiredDate.isBefore(capability.getValidFrom())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_CAPABILITY_NOT_ACTIVE);
        }

        if (requestedQty.compareTo(capability.getMoq()) < 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_MOQ_NOT_MET);
        }

        BigDecimal allocatedOpenQty =
                subPurchaseOrderItemRepository.sumOrderedQtyBySupplierIdAndItemIdAndLineStatusIn(
                        targetSupplierId,
                        itemId,
                        ACTIVE_LINE_STATUSES
                );

        if (allocatedOpenQty.add(requestedQty).compareTo(capability.getAvailableQty()) > 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_AVAILABLE_QTY_EXCEEDED);
        }

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        BigDecimal allocatedMonthQty =
                subPurchaseOrderItemRepository.sumMonthlyOrderedQtyBySupplierIdAndItemIdAndOrderedAtBetweenAndLineStatusIn(
                        targetSupplierId,
                        itemId,
                        monthStart,
                        nextMonthStart,
                        ACTIVE_LINE_STATUSES
                );

        if (allocatedMonthQty.add(requestedQty).compareTo(capability.getMonthlyCapacity()) > 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_MONTHLY_CAPACITY_EXCEEDED);
        }

        LocalDate earliestAvailableDate = LocalDate.now().plusDays(capability.getLeadTimeDays());
        if (requiredDate.isBefore(earliestAvailableDate)) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_LEAD_TIME_NOT_MET);
        }
    }

    private SupplySubPurchaseOrder getReceiverOwnedSubPurchaseOrder(
            String receiverOrganizationPublicId,
            String subPoPublicId
    ) {
        return subPurchaseOrderRepository
                .findByPublicIdAndSupplier_OrganizationPublicIdAndSubPoStatusNot(
                        subPoPublicId,
                        receiverOrganizationPublicId,
                        SubPoStatus.DELETED
                )
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ACCESS_DENIED));
    }

    private void validateReceiverActionable(SupplySubPurchaseOrder subPurchaseOrder) {
        if (subPurchaseOrder.getSubPoStatus() != SubPoStatus.CREATED) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_STATUS_NOT_ALLOWED);
        }
    }

    private void validateReceiverConfirmable(SupplySubPurchaseOrder subPurchaseOrder) {
        if (!(subPurchaseOrder.getSubPoStatus() == SubPoStatus.ACCEPTED
                || subPurchaseOrder.getSubPoStatus() == SubPoStatus.PARTIALLY_CONFIRMED
                || subPurchaseOrder.getSubPoStatus() == SubPoStatus.CONFIRMED)) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_STATUS_NOT_ALLOWED);
        }
    }
}
