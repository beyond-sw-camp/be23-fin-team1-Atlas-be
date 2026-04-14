package com.ozz.atlas.supply.subpurchaseorder.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
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

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SUPPLIER_ORGANIZATION_TYPE = "SUPPLIER";

    private static final List<SubPurchaseOrderLineStatus> ACTIVE_LINE_STATUSES = List.of(
            SubPurchaseOrderLineStatus.OPEN,
            SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED,
            SubPurchaseOrderLineStatus.CONFIRMED
    );

    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SubPurchaseOrderItemRepository subPurchaseOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierItemCapabilityRepository capabilityRepository;
    private final SupplyItemRepository supplyItemRepository;

    public SubPurchaseOrderResponse createSubPurchaseOrder(
            String issuerOrganizationPublicId,
            String organizationType,
            String createdByUserPublicId,
            CreateSubPurchaseOrderRequest request
    ) {
        validateSupplierActor(issuerOrganizationPublicId, organizationType);
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

        if (subPurchaseOrderRepository.existsBySubPoNumberAndParentPurchaseOrder_IdAndSubPoStatusNot(
                request.getSubPoNumber(),
                parentPurchaseOrder.getId(),
                SubPoStatus.DELETED
        )) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_NUMBER_ALREADY_EXISTS);
        }

        SupplySupplier issuerSupplier = parentPurchaseOrder.getSupplier();

        SupplySupplier targetSupplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        request.getSupplierPublicId(),
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.TARGET_SUPPLIER_NOT_FOUND));

        if (targetSupplier.getId().equals(issuerSupplier.getId())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_SAME_SUPPLIER_NOT_ALLOWED);
        }

        validateTargetSupplierTier(issuerSupplier, targetSupplier);

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
            String organizationPublicId,
            String organizationType,
            String userRole,
            String parentPoPublicId,
            Pageable pageable
    ) {
        if (isAdmin(userRole)) {
            return subPurchaseOrderRepository
                    .findAllByParentPurchaseOrder_PublicIdAndSubPoStatusNot(
                            parentPoPublicId,
                            SubPoStatus.DELETED,
                            pageable
                    )
                    .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
        }

        validateSupplierActor(organizationPublicId, organizationType);

        return subPurchaseOrderRepository
                .findAllByParentPurchaseOrder_PublicIdAndParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
                        parentPoPublicId,
                        organizationPublicId,
                        SubPoStatus.DELETED,
                        pageable
                )
                .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
    }

    @Transactional(readOnly = true)
    public Page<SubPurchaseOrderResponse> getReceivedSubPurchaseOrders(
            String organizationPublicId,
            String organizationType,
            String userRole,
            Pageable pageable
    ) {
        if (isAdmin(userRole)) {
            return subPurchaseOrderRepository
                    .findAllBySubPoStatusNot(SubPoStatus.DELETED, pageable)
                    .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
        }

        validateSupplierActor(organizationPublicId, organizationType);

        return subPurchaseOrderRepository
                .findAllBySupplier_OrganizationPublicIdAndSubPoStatusNot(
                        organizationPublicId,
                        SubPoStatus.DELETED,
                        pageable
                )
                .map(subPo -> SubPurchaseOrderResponse.fromEntity(subPo, false));
    }

    @Transactional(readOnly = true)
    public SubPurchaseOrderResponse getSubPurchaseOrder(
            String organizationPublicId,
            String organizationType,
            String userRole,
            String subPoPublicId
    ) {
        if (isAdmin(userRole)) {
            SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository
                    .findByPublicIdAndSubPoStatusNot(subPoPublicId, SubPoStatus.DELETED)
                    .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_NOT_FOUND));

            return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
        }

        validateSupplierActor(organizationPublicId, organizationType);

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
            String organizationType,
            String subPoPublicId
    ) {
        validateSupplierActor(receiverOrganizationPublicId, organizationType);

        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverActionable(subPurchaseOrder);

        subPurchaseOrder.accept();
        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    public SubPurchaseOrderResponse rejectSubPurchaseOrder(
            String receiverOrganizationPublicId,
            String organizationType,
            String subPoPublicId
    ) {
        validateSupplierActor(receiverOrganizationPublicId, organizationType);

        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverActionable(subPurchaseOrder);

        subPurchaseOrder.reject();
        return SubPurchaseOrderResponse.fromEntity(subPurchaseOrder, true);
    }

    public SubPurchaseOrderResponse confirmSubPurchaseOrderItem(
            String receiverOrganizationPublicId,
            String organizationType,
            String subPoPublicId,
            String parentPoItemPublicId,
            String itemPublicId,
            ConfirmSubPurchaseOrderItemRequest request
    ) {
        validateSupplierActor(receiverOrganizationPublicId, organizationType);

        SupplySubPurchaseOrder subPurchaseOrder = getReceiverOwnedSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId);
        validateReceiverConfirmable(subPurchaseOrder);

        SupplySubPurchaseOrderItem item = subPurchaseOrder.findActiveItem(parentPoItemPublicId, itemPublicId);
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
                .map(item -> item.getParentPoItemPublicId() + "::" + item.getItemPublicId())
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

        SupplyItem subItem = getActiveItem(request.getItemPublicId());
        validateItemBelongsToSupplier(subItem, targetSupplier);

        BigDecimal requestedQty = request.getOrderedQty();

        LocalDate requiredDate = request.getRequiredDate() != null
                ? request.getRequiredDate()
                : parentItem.getRequiredDate();

        if (requiredDate.isAfter(subPoDueDate) || requiredDate.isAfter(parentItem.getRequiredDate())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        validateSupplierCapability(targetSupplier.getId(), subItem, requestedQty, requiredDate);

        return SupplySubPurchaseOrderItem.create(
                parentItem,
                subItem,
                requestedQty,
                requiredDate
        );
    }

    private void validateSupplierCapability(
            Long targetSupplierId,
            SupplyItem subItem,
            BigDecimal requestedQty,
            LocalDate requiredDate
    ) {
        Long itemId = subItem.getId();

        SupplySupplierItemCapability capability = capabilityRepository
                .findBySupplier_IdAndItem_Id(targetSupplierId, itemId)
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.TARGET_SUPPLIER_CAPABILITY_NOT_FOUND));

        if (capability.getValidFrom() != null && requiredDate.isBefore(capability.getValidFrom())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_CAPABILITY_NOT_ACTIVE);
        }

        if (requestedQty.compareTo(capability.getMoq()) < 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_MOQ_NOT_MET);
        }

        BigDecimal allocatedOpenQty = subPurchaseOrderItemRepository.sumOrderedQtyBySupplierIdAndItemIdAndLineStatusIn(
                targetSupplierId,
                itemId,
                ACTIVE_LINE_STATUSES
        );

        if (allocatedOpenQty.add(requestedQty).compareTo(capability.getAvailableQty()) > 0) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_AVAILABLE_QTY_EXCEEDED);
        }

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        BigDecimal allocatedMonthQty = subPurchaseOrderItemRepository
                .sumMonthlyOrderedQtyBySupplierIdAndItemIdAndOrderedAtBetweenAndLineStatusIn(
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

    private SupplyItem getActiveItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(itemPublicId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ITEM_NOT_FOUND));
    }

    private void validateItemBelongsToSupplier(SupplyItem item, SupplySupplier supplier) {
        if (!item.getSupplier().getId().equals(supplier.getId())) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ITEM_SUPPLIER_MISMATCH);
        }
    }

    private void validateTargetSupplierTier(SupplySupplier issuerSupplier, SupplySupplier targetSupplier) {
        if (targetSupplier.getTierLevel().getOrder() <= issuerSupplier.getTierLevel().getOrder()) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.TARGET_SUPPLIER_TIER_NOT_ALLOWED);
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

    private void validateSupplierActor(String organizationPublicId, String organizationType) {
        validateOrganizationHeader(organizationPublicId);

        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.SUB_PURCHASE_ORDER_ACCESS_DENIED);
        }
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new SubPurchaseOrderException(SubPurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }
}
