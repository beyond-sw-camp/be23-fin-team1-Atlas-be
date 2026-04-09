package com.ozz.atlas.supply.purchaseorder.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.dtos.ChangePurchaseOrderStatusRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.ConfirmPurchaseOrderItemRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.CreatePurchaseOrderItemRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.CreatePurchaseOrderRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.PurchaseOrderDetailResponse;
import com.ozz.atlas.supply.purchaseorder.dtos.PurchaseOrderSummaryResponse;
import com.ozz.atlas.supply.purchaseorder.dtos.UpdatePurchaseOrderItemRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.UpdatePurchaseOrderRequest;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderErrorCode;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderException;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderItemRepository;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private static final List<SubPurchaseOrderLineStatus> ACTIVE_SUB_ORDER_LINE_STATUSES = List.of(
            SubPurchaseOrderLineStatus.OPEN,
            SubPurchaseOrderLineStatus.PARTIALLY_CONFIRMED,
            SubPurchaseOrderLineStatus.CONFIRMED
    );

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;
    private final SubPurchaseOrderItemRepository subPurchaseOrderItemRepository;

    public PurchaseOrderDetailResponse createPurchaseOrder(
            String buyerOrganizationPublicId,
            String createdByUserPublicId,
            CreatePurchaseOrderRequest request
    ) {
        validateCreateRequest(request);

        if (purchaseOrderRepository.existsByPoNumberAndBuyerOrganizationPublicIdAndPoStatusNot(
                request.getPoNumber(),
                buyerOrganizationPublicId,
                PoStatus.DELETED
        )) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_NUMBER_ALREADY_EXISTS);
        }

        SupplySupplier supplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        request.getSupplierPublicId(),
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SUPPLIER_NOT_FOUND));

        Map<String, SupplyItem> itemMap = resolveItems(
                request.getItems().stream()
                        .map(CreatePurchaseOrderItemRequest::getItemPublicId)
                        .toList()
        );

        List<SupplyPurchaseOrderItem> purchaseOrderItems = request.getItems().stream()
                .map(itemRequest -> {
                    LocalDate requiredDate = itemRequest.getRequiredDate() != null
                            ? itemRequest.getRequiredDate()
                            : request.getDueDate();

                    if (requiredDate.isAfter(request.getDueDate())) {
                        throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
                    }

                    return SupplyPurchaseOrderItem.create(
                            itemMap.get(itemRequest.getItemPublicId()),
                            itemRequest.getOrderedQty(),
                            itemRequest.getUnitPrice(),
                            requiredDate
                    );
                })
                .toList();

        SupplyPurchaseOrder purchaseOrder = SupplyPurchaseOrder.create(
                request.getPoNumber(),
                buyerOrganizationPublicId,
                supplier,
                request.getPriorityCode(),
                request.getDueDate(),
                request.getCurrencyCode(),
                request.getMemo(),
                createdByUserPublicId,
                purchaseOrderItems
        );

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> getPurchaseOrderList(
            String organizationPublicId,
            PurchaseOrderViewType viewType,
            String supplierPublicId,
            Pageable pageable
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank() || viewType == null) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        // 구매사 관점 조회 (supplierPublicId 있으면 안됨)
        if (viewType == PurchaseOrderViewType.BUYER) {
            if (supplierPublicId != null && !supplierPublicId.isBlank()) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
            }

            return purchaseOrderRepository.findAllByBuyerOrganizationPublicIdAndPoStatusNot(
                            organizationPublicId,
                            PoStatus.DELETED,
                            pageable
                    )
                    .map(PurchaseOrderSummaryResponse::fromEntity);
        }

        // 공급사 관점
        if (supplierPublicId != null && !supplierPublicId.isBlank()) {
            return purchaseOrderRepository
                    .findAllBySupplier_OrganizationPublicIdAndSupplier_PublicIdAndPoStatusNot(
                            organizationPublicId,
                            supplierPublicId,
                            PoStatus.DELETED,
                            pageable
                    )
                    .map(PurchaseOrderSummaryResponse::fromEntity);
        }

        return purchaseOrderRepository.findAllBySupplier_OrganizationPublicIdAndPoStatusNot(
                        organizationPublicId,
                        PoStatus.DELETED,
                        pageable
                )
                .map(PurchaseOrderSummaryResponse::fromEntity);
    }


    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrder(String poPublicId) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                        poPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_NOT_FOUND));

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse updatePurchaseOrder(
            String buyerOrganizationPublicId,
            String poPublicId,
            UpdatePurchaseOrderRequest request
    ) {
        // 발주 조회 (발주사 맞지 않으면 예외)
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        // request 비어 있는지 검증
        if (isEmptyHeaderPatch(request)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        // 하위 발주 존재 여부 검증 (활성 하위 발주 있으면 예외)
        // 외부 입력은 publicId, 내부 검증은 PK 기준으로 처리
        validateNoActiveSubOrdersForPurchaseOrder(purchaseOrder.getId());
        // 발주 상태가 CREATED일 때만 수정 가능
        validateBuyerEditable(purchaseOrder);

        // 발주번호 중복 체크
        if (request.getPoNumber() != null
                && !request.getPoNumber().isBlank()
                && !request.getPoNumber().equals(purchaseOrder.getPoNumber())
                && purchaseOrderRepository.existsByPoNumberAndBuyerOrganizationPublicIdAndIdNotAndPoStatusNot(
                request.getPoNumber(),
                buyerOrganizationPublicId,
                purchaseOrder.getId(),
                PoStatus.DELETED
        )) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_NUMBER_ALREADY_EXISTS);
        }

        // dueDate(납기일) 변경 시, 기존 아이템 requiredDate(요청 납기일) 검증 (requiredDate ≤ dueDate)
        if (request.getDueDate() != null) {
            boolean invalidRequiredDate = purchaseOrder.getActiveItems().stream()
                    .anyMatch(item -> item.getRequiredDate().isAfter(request.getDueDate()));

            if (invalidRequiredDate) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
            }
        }

        purchaseOrder.updateHeader(
                request.getPoNumber(),
                request.getPriorityCode(),
                request.getDueDate(),
                request.getMemo()
        );

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    // 부모 발주를 받은 협력사가 상위 발주를 수락한다.
    public PurchaseOrderDetailResponse acceptPurchaseOrder(
            String supplierOrganizationPublicId,
            String poPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = getSupplierOwnedPurchaseOrder(supplierOrganizationPublicId, poPublicId);
        validateSupplierActionable(purchaseOrder);

        purchaseOrder.accept();
        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse rejectPurchaseOrder(
            String supplierOrganizationPublicId,
            String poPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = getSupplierOwnedPurchaseOrder(supplierOrganizationPublicId, poPublicId);
        validateSupplierActionable(purchaseOrder);

        purchaseOrder.reject();
        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse changePurchaseOrderStatus(
            String buyerOrganizationPublicId,
            String poPublicId,
            ChangePurchaseOrderStatusRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        if (request.getPoStatus() == PoStatus.CANCELLED) {
            validateNoActiveSubOrdersForPurchaseOrder(purchaseOrder.getId());
            purchaseOrder.cancel();
            return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
        }

        if (request.getPoStatus() == PoStatus.COMPLETED) {
            if (purchaseOrder.getPoStatus() != PoStatus.CONFIRMED) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_STATUS_CHANGE_NOT_ALLOWED);
            }
            purchaseOrder.complete();
            return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
        }

        throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_STATUS_CHANGE_NOT_ALLOWED);
    }

    public void deletePurchaseOrder(String buyerOrganizationPublicId, String poPublicId) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        validateNoActiveSubOrdersForPurchaseOrder(purchaseOrder.getId());
        purchaseOrder.delete();
    }

    public PurchaseOrderDetailResponse addPurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            CreatePurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        validateBuyerEditable(purchaseOrder);

        SupplyItem item = resolveSingleItem(request.getItemPublicId());

        // 요청은 itemPublicId로 들어오지만, 엔티티를 찾은 뒤 중복 비교는 내부 PK로 처리한다.
        boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                .anyMatch(existingItem -> existingItem.getItem().getId().equals(item.getId()));

        if (duplicatedItem) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
        }

        LocalDate requiredDate = request.getRequiredDate() != null
                ? request.getRequiredDate()
                : purchaseOrder.getDueDate();

        if (requiredDate.isAfter(purchaseOrder.getDueDate())) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        purchaseOrder.addItem(
                SupplyPurchaseOrderItem.create(
                        item,
                        request.getOrderedQty(),
                        request.getUnitPrice(),
                        requiredDate
                )
        );

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse updatePurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            String poItemPublicId,
            UpdatePurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        if (isEmptyItemPatch(request)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        // API path는 poItemPublicId를 쓰지만, 엔티티를 찾은 뒤 내부 검증은 poItemId로 처리한다.
        SupplyPurchaseOrderItem purchaseOrderItem = findActivePurchaseOrderItem(purchaseOrder, poItemPublicId);

        validateNoActiveSubOrdersForPurchaseOrderItem(purchaseOrderItem.getPoItemId());
        validateBuyerEditable(purchaseOrder);

        SupplyItem targetItem = purchaseOrderItem.getItem();
        if (request.getItemPublicId() != null && !request.getItemPublicId().isBlank()) {
            SupplyItem requestedItem = resolveSingleItem(request.getItemPublicId());

            boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                    .filter(item -> !item.getPoItemId().equals(purchaseOrderItem.getPoItemId()))
                    .anyMatch(item -> item.getItem().getId().equals(requestedItem.getId()));

            if (duplicatedItem) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
            }

            targetItem = requestedItem;
        }

        LocalDate requiredDate = request.getRequiredDate() != null
                ? request.getRequiredDate()
                : purchaseOrderItem.getRequiredDate();

        if (requiredDate.isAfter(purchaseOrder.getDueDate())) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        purchaseOrderItem.update(
                targetItem,
                request.getOrderedQty() != null ? request.getOrderedQty() : purchaseOrderItem.getOrderedQty(),
                request.getUnitPrice() != null ? request.getUnitPrice() : purchaseOrderItem.getUnitPrice(),
                requiredDate
        );

        purchaseOrder.refreshAfterItemChanged();
        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public void deletePurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            String poItemPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        SupplyPurchaseOrderItem purchaseOrderItem = findActivePurchaseOrderItem(purchaseOrder, poItemPublicId);

        validateNoActiveSubOrdersForPurchaseOrderItem(purchaseOrderItem.getPoItemId());
        validateBuyerEditable(purchaseOrder);

        if (purchaseOrder.getActiveItems().size() <= 1) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_MINIMUM_REQUIRED);
        }

        purchaseOrderItem.delete();
        purchaseOrder.refreshAfterItemChanged();
    }

    public PurchaseOrderDetailResponse confirmPurchaseOrderItem(
            String supplierOrganizationPublicId,
            String poPublicId,
            String poItemPublicId,
            ConfirmPurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getSupplierOwnedPurchaseOrder(supplierOrganizationPublicId, poPublicId);
        validateSupplierConfirmable(purchaseOrder);

        SupplyPurchaseOrderItem purchaseOrderItem = findActivePurchaseOrderItem(purchaseOrder, poItemPublicId);

        if (request.getConfirmedQty().compareTo(purchaseOrderItem.getOrderedQty()) > 0) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_CONFIRM_QTY_INVALID);
        }

        validateConfirmedQtyAgainstAllocatedSubOrders(purchaseOrderItem.getPoItemId(), request.getConfirmedQty());

        purchaseOrderItem.confirm(request.getConfirmedQty());
        purchaseOrder.refreshConfirmationStatus();

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    // 발주 생성 요청 검증
    private void validateCreateRequest(CreatePurchaseOrderRequest request) {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_EMPTY);
            }

            long distinctItemCount = request.getItems().stream()
                    .map(CreatePurchaseOrderItemRequest::getItemPublicId)
                    .distinct()
                    .count();

            if (distinctItemCount != request.getItems().size()) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
        }
    }

    private Map<String, SupplyItem> resolveItems(List<String> itemPublicIds) {
        List<SupplyItem> items = supplyItemRepository.findAllByPublicIdInAndStatus(itemPublicIds, Status.ACTIVE);

        if (items.size() != itemPublicIds.size()) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(SupplyItem::getPublicId, Function.identity()));
    }

    private SupplyItem resolveSingleItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(itemPublicId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND));
    }

    private SupplyPurchaseOrder getBuyerOwnedPurchaseOrder(String buyerOrganizationPublicId, String poPublicId) {
        return purchaseOrderRepository.findByPublicIdAndBuyerOrganizationPublicIdAndPoStatusNot(
                        poPublicId,
                        buyerOrganizationPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ACCESS_DENIED));
    }

    private SupplyPurchaseOrder getSupplierOwnedPurchaseOrder(String supplierOrganizationPublicId, String poPublicId) {
        return purchaseOrderRepository.findByPublicIdAndSupplier_OrganizationPublicIdAndPoStatusNot(
                        poPublicId,
                        supplierOrganizationPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ACCESS_DENIED));
    }

    private void validateBuyerEditable(SupplyPurchaseOrder purchaseOrder) {
        if (purchaseOrder.getPoStatus() != PoStatus.CREATED) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_BUYER_EDIT_NOT_ALLOWED);
        }
    }

    private void validateSupplierActionable(SupplyPurchaseOrder purchaseOrder) {
        if (purchaseOrder.getPoStatus() != PoStatus.CREATED) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SUPPLIER_ACTION_NOT_ALLOWED);
        }
    }

    private void validateSupplierConfirmable(SupplyPurchaseOrder purchaseOrder) {
        if (!(purchaseOrder.getPoStatus() == PoStatus.ACCEPTED
                || purchaseOrder.getPoStatus() == PoStatus.PARTIALLY_CONFIRMED
                || purchaseOrder.getPoStatus() == PoStatus.CONFIRMED)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SUPPLIER_ACTION_NOT_ALLOWED);
        }
    }

    private boolean isEmptyHeaderPatch(UpdatePurchaseOrderRequest request) {
        return (request.getPoNumber() == null || request.getPoNumber().isBlank())
                && request.getPriorityCode() == null
                && request.getDueDate() == null
                && request.getMemo() == null;
    }

    private boolean isEmptyItemPatch(UpdatePurchaseOrderItemRequest request) {
        return (request.getItemPublicId() == null || request.getItemPublicId().isBlank())
                && request.getOrderedQty() == null
                && request.getUnitPrice() == null
                && request.getRequiredDate() == null;
    }

    private SupplyPurchaseOrderItem findActivePurchaseOrderItem(
            SupplyPurchaseOrder purchaseOrder,
            String poItemPublicId
    ) {
        return purchaseOrder.getActiveItems().stream()
                .filter(item -> item.getPublicId().equals(poItemPublicId))
                .findFirst()
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND));
    }

    private void validateNoActiveSubOrdersForPurchaseOrder(Long poId) {
        boolean hasActiveSubOrders = subPurchaseOrderItemRepository
                .existsActiveByParentPurchaseOrderIdAndLineStatusIn(
                        poId,
                        ACTIVE_SUB_ORDER_LINE_STATUSES
                );

        if (hasActiveSubOrders) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_HAS_ACTIVE_SUB_PURCHASE_ORDER);
        }
    }

    private void validateNoActiveSubOrdersForPurchaseOrderItem(Long poItemId) {
        boolean hasActiveSubOrders = subPurchaseOrderItemRepository
                .existsActiveByParentPurchaseOrderItemIdAndLineStatusIn(
                        poItemId,
                        ACTIVE_SUB_ORDER_LINE_STATUSES
                );

        if (hasActiveSubOrders) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_HAS_ACTIVE_SUB_PURCHASE_ORDER);
        }
    }

    private void validateConfirmedQtyAgainstAllocatedSubOrders(Long poItemId, BigDecimal confirmedQty) {
        BigDecimal allocatedSubOrderQty = subPurchaseOrderItemRepository
                .sumOrderedQtyByParentPurchaseOrderItemIdAndLineStatusIn(
                        poItemId,
                        ACTIVE_SUB_ORDER_LINE_STATUSES
                );

        if (confirmedQty.compareTo(allocatedSubOrderQty) < 0) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_CONFIRM_QTY_LESS_THAN_SUB_ORDER_QTY);
        }
    }
}
