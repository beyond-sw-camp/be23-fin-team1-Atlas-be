package com.ozz.atlas.supply.purchaseorder.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.dtos.*;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderErrorCode;
import com.ozz.atlas.supply.purchaseorder.exception.PurchaseOrderException;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
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

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;

    public PurchaseOrderDetailResponse createPurchaseOrder(String buyerOrganizationPublicId, String createdByUserPublicId, CreatePurchaseOrderRequest request) {
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
                        .map(CreatePurchaseOrderItemRequest:: getItemPublicId)
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

    // 발주 목록 조회
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> getPurchaseOrderList(String buyerOrganizationPublicId,
                                                                String supplierPublicId,
                                                                Pageable pageable) {
        // 협력사 조회
        if (supplierPublicId != null && !supplierPublicId.isBlank()) {
            return purchaseOrderRepository.findAllBySupplier_PublicIdAndPoStatusNot(
                            supplierPublicId,
                            PoStatus.DELETED,
                            pageable
                    )
                    .map(PurchaseOrderSummaryResponse::fromEntity);
        }

        // 발주사 조회
        if (buyerOrganizationPublicId != null && !buyerOrganizationPublicId.isBlank()) {
            return purchaseOrderRepository.findAllByBuyerOrganizationPublicIdAndPoStatusNot(
                            buyerOrganizationPublicId,
                            PoStatus.DELETED,
                            pageable
                    )
                    .map(PurchaseOrderSummaryResponse::fromEntity);
        }

        throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
    }

    // 발주 단건 조회
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrder(String poPublicId) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                        poPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_NOT_FOUND));

        return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
    }

    public PurchaseOrderDetailResponse updatePurchaseOrder(String buyerOrganizationPublicId,
                                                           String poPublicId,
                                                           UpdatePurchaseOrderRequest request) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        validateBuyerEditable(purchaseOrder);

        if (isEmptyHeaderPatch(request)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getPoNumber() != null
                && !request.getPoNumber().isBlank()
                && !request.getPoNumber().equals(purchaseOrder.getPoNumber())
                && purchaseOrderRepository.existsByPoNumberAndBuyerOrganizationPublicIdAndPublicIdNotAndPoStatusNot(
                request.getPoNumber(),
                buyerOrganizationPublicId,
                poPublicId,
                PoStatus.DELETED
        )) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_NUMBER_ALREADY_EXISTS);
        }

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

    public PurchaseOrderDetailResponse changePurchaseOrderStatus(
            String buyerOrganizationPublicId,
            String poPublicId,
            ChangePurchaseOrderStatusRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);

        if (request.getPoStatus() == PoStatus.CANCELLED) {
            purchaseOrder.cancel();
            return PurchaseOrderDetailResponse.fromEntity(purchaseOrder);
        }

        // 완료는 적어도 모든 상세가 확정된 뒤에만 허용한다.
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
        purchaseOrder.delete();
    }

    public PurchaseOrderDetailResponse addPurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            CreatePurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        validateBuyerEditable(purchaseOrder);

        // 한 발주 안에 같은 품목을 두 줄로 넣지 않게 막는다.
        boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                .anyMatch(item -> item.getItem().getPublicId().equals(request.getItemPublicId()));

        if (duplicatedItem) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
        }

        SupplyItem item = resolveSingleItem(request.getItemPublicId());

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

    // 발주 상세 수정, CREATED에서만 허용
    public PurchaseOrderDetailResponse updatePurchaseOrderItem(
            String buyerOrganizationPublicId,
            String poPublicId,
            String poItemPublicId,
            UpdatePurchaseOrderItemRequest request
    ) {
        SupplyPurchaseOrder purchaseOrder = getBuyerOwnedPurchaseOrder(buyerOrganizationPublicId, poPublicId);
        validateBuyerEditable(purchaseOrder);

        if (isEmptyItemPatch(request)) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.INVALID_INPUT_VALUE);
        }

        SupplyPurchaseOrderItem purchaseOrderItem = findActivePurchaseOrderItem(purchaseOrder, poItemPublicId);

        SupplyItem targetItem = purchaseOrderItem.getItem();
        if (request.getItemPublicId() != null && !request.getItemPublicId().isBlank()) {
            // 수정 대상 라인을 제외하고 같은 품목이 이미 있으면 중복이다.
            boolean duplicatedItem = purchaseOrder.getActiveItems().stream()
                    .filter(item -> !item.getPublicId().equals(poItemPublicId))
                    .anyMatch(item -> item.getItem().getPublicId().equals(request.getItemPublicId()));

            if (duplicatedItem) {
                throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_DUPLICATE_ITEM);
            }

            targetItem = resolveSingleItem(request.getItemPublicId());
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

    // 발주 생성 전에 상세 개수/중복 품목을 막는 1차 검증
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

    // itemPublicId 리스트 -> SupplyItem으로 변환
    private Map<String, SupplyItem> resolveItems(List<String> itemPublicIds) {
        List<SupplyItem> items = supplyItemRepository.findAllByPublicIdInAndStatus(itemPublicIds, Status.ACTIVE);

        if (items.size() != itemPublicIds.size()) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(SupplyItem::getPublicId, Function.identity()));
    }

    // buyer 조직 소유 발주만 수정하게 하는 접근 제한
    private SupplyPurchaseOrder getBuyerOwnedPurchaseOrder(String buyerOrganizationPublicId, String poPublicId) {
        return purchaseOrderRepository.findByPublicIdAndBuyerOrganizationPublicIdAndPoStatusNot(
                        poPublicId,
                        buyerOrganizationPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ACCESS_DENIED));
    }

    // 발주 상태가 CREATED일 때만 수정 가능
    private void validateBuyerEditable(SupplyPurchaseOrder purchaseOrder) {
        if (purchaseOrder.getPoStatus() != PoStatus.CREATED) {
            throw new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_BUYER_EDIT_NOT_ALLOWED);
        }
    }

    // 빈 값 체크
    private boolean isEmptyHeaderPatch(UpdatePurchaseOrderRequest request) {
        return (request.getPoNumber() == null || request.getPoNumber().isBlank())
                && request.getPriorityCode() == null
                && request.getDueDate() == null
                && request.getMemo() == null;
    }

    private SupplyItem resolveSingleItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(itemPublicId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new PurchaseOrderException(PurchaseOrderErrorCode.PURCHASE_ORDER_ITEM_NOT_FOUND));
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

}
