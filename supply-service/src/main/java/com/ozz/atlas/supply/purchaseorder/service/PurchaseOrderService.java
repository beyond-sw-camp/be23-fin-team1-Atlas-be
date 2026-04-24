package com.ozz.atlas.supply.purchaseorder.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.common.code.SequenceCodeType;
import com.ozz.atlas.supply.common.code.YearlySequenceCodeGenerator;
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
import com.ozz.atlas.supply.purchaseorder.search.service.PurchaseOrderSearchService;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PurchaseOrderSearchService purchaseOrderSearchService;
    private final SupplierItemCapabilityRepository supplierItemCapabilityRepository;


    public PurchaseOrderDetailResponse createPurchaseOrder(
                        String buyerOrganizationPublicId,
                        String createdByUserPublicId,
                        CreatePurchaseOrderRequest request
                ) {
            validateCreateRequest(request);

        SupplySupplier supplier = supplierRepository.findByPublicIdAndSupplierStatusNot(
                        request.getSupplierPublicId(),
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SUPPLIER_NOT_FOUND));

        if (buyerOrganizationPublicId.equals(supplier.getOrganizationPublicId())) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SELF_SUPPLIER_NOT_ALLOWED);
        }

        Map<String, SupplyItem> itemMap = resolveItems(
                request.getItems().stream()
                        .map(CreatePurchaseOrderItemRequest::getItemPublicId)
                        .toList()
        );


        validateItemsBelongToSupplier(itemMap.values(), supplier);

        String poNumber = generateNextPoNumber();

        List<SupplyPurchaseOrderItem> purchaseOrderItems = request.getItems().stream()
                .map(itemRequest -> {
                    SupplyItem item = itemMap.get(itemRequest.getItemPublicId());
                    SupplySupplierItemCapability capability = getCapability(supplier, item);

                    return SupplyPurchaseOrderItem.create(
                            item,
                            itemRequest.getOrderedQty(),
                            item.getUnitPrice(),
                            capability.getLeadTimeDays(),
                            capability.getPartialConfirmationAllowed(),
                            calculateExpectedDueDate(capability)
                    );

                })
                .toList();

        SupplyPurchaseOrder purchaseOrder = SupplyPurchaseOrder.create(
                poNumber,
                buyerOrganizationPublicId,
                supplier,
                request.getCurrencyCode(),
                request.getMemo(),
                createdByUserPublicId,
                purchaseOrderItems
        );


        SupplyPurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        purchaseOrderSearchService.savePurchaseOrderDocument(savedPurchaseOrder);
        return PurchaseOrderDetailResponse.fromEntity(savedPurchaseOrder);
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

        purchaseOrder.updateHeader(request.getMemo());

        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse rejectPurchaseOrder(
            String supplierOrganizationPublicId,
            String poPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = getSupplierOwnedPurchaseOrder(supplierOrganizationPublicId, poPublicId);
        validateSupplierActionable(purchaseOrder);

        purchaseOrder.reject();
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
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
            purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
            return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
        }

        if (request.getPoStatus() == PoStatus.COMPLETED) {
            if (purchaseOrder.getPoStatus() != PoStatus.CONFIRMED) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_STATUS_CHANGE_NOT_ALLOWED);
            }
            purchaseOrder.complete();
            purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
            return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
        }

        throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_STATUS_CHANGE_NOT_ALLOWED);
    }

    public void deletePurchaseOrder(String buyerOrganizationPublicId, String poPublicId) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        validateNoActiveSubOrdersForPurchaseOrder(purchaseOrder.getId());
        purchaseOrder.delete();
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
    }

    public PurchaseOrderDetailResponse addPurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            CreatePurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        validateBuyerEditable(purchaseOrder);

        SupplyItem item = resolveSingleItem(request.getItemPublicId());
        validateItemBelongsToSupplier(item, purchaseOrder.getSupplier());

        boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                .anyMatch(existingItem -> existingItem.getItem().getId().equals(item.getId()));

        if (duplicatedItem) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
        }

        SupplySupplierItemCapability capability = getCapability(purchaseOrder.getSupplier(), item);

        purchaseOrder.addItem(
                SupplyPurchaseOrderItem.create(
                        item,
                        request.getOrderedQty(),
                        item.getUnitPrice(),
                        capability.getLeadTimeDays(),
                        capability.getPartialConfirmationAllowed(),
                        calculateExpectedDueDate(capability)
                )
        );
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
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
            validateItemBelongsToSupplier(requestedItem, purchaseOrder.getSupplier());


            boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                    .filter(item -> !item.getPoItemId().equals(purchaseOrderItem.getPoItemId()))
                    .anyMatch(item -> item.getItem().getId().equals(requestedItem.getId()));

            if (duplicatedItem) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
            }

            targetItem = requestedItem;
        }

        SupplySupplierItemCapability capability = getCapability(purchaseOrder.getSupplier(), targetItem);

        purchaseOrderItem.update(
                targetItem,
                request.getOrderedQty() != null ? request.getOrderedQty() : purchaseOrderItem.getOrderedQty(),
                targetItem.getUnitPrice(),
                capability.getLeadTimeDays(),
                capability.getPartialConfirmationAllowed(),
                calculateExpectedDueDate(capability)
        );



        purchaseOrder.refreshAfterItemChanged();
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
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
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
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

        purchaseOrderItem.confirm(request.getConfirmedQty());
        purchaseOrder.refreshConfirmationStatus();
        purchaseOrderSearchService.savePurchaseOrderDocument(purchaseOrder);
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
        if (!(purchaseOrder.getPoStatus() == PoStatus.CREATED
                || purchaseOrder.getPoStatus() == PoStatus.PARTIALLY_CONFIRMED
                || purchaseOrder.getPoStatus() == PoStatus.CONFIRMED)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_SUPPLIER_ACTION_NOT_ALLOWED);
        }
    }

    private boolean isEmptyHeaderPatch(UpdatePurchaseOrderRequest request) {
        return request.getMemo() == null;
    }

    private boolean isEmptyItemPatch(UpdatePurchaseOrderItemRequest request) {
        return (request.getItemPublicId() == null || request.getItemPublicId().isBlank())
                && request.getOrderedQty() == null;
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

    private void validateItemsBelongToSupplier(Iterable<SupplyItem> items, SupplySupplier supplier) {
        for (SupplyItem item : items) {
            validateItemBelongsToSupplier(item, supplier);
        }
    }

    private void validateItemBelongsToSupplier(SupplyItem item, SupplySupplier supplier) {
        if (!item.getSupplier().getId().equals(supplier.getId())) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_SUPPLIER_MISMATCH);
        }
    }

    private SupplySupplierItemCapability getCapability(SupplySupplier supplier, SupplyItem item) {
        return supplierItemCapabilityRepository.findBySupplier_IdAndItem_Id(supplier.getId(), item.getId())
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_CAPABILITY_NOT_FOUND));
    }

    private LocalDate calculateExpectedDueDate(SupplySupplierItemCapability capability) {
        return LocalDate.now().plusDays(capability.getLeadTimeDays());
    }

    private String generateNextPoNumber() {
        String prefix = YearlySequenceCodeGenerator.currentPrefix(SequenceCodeType.PURCHASE_ORDER);
        String lastCode = purchaseOrderRepository.findTopByPoNumberStartingWithOrderByPoNumberDesc(prefix)
                .map(SupplyPurchaseOrder::getPoNumber)
                .orElse(null);

        String candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.PURCHASE_ORDER, lastCode, 7);
        while (purchaseOrderRepository.existsByPoNumber(candidate)) {
            candidate = YearlySequenceCodeGenerator.next(SequenceCodeType.PURCHASE_ORDER, candidate, 7);
        }
        return candidate;
    }




}
