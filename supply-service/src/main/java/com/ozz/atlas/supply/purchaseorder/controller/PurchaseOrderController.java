package com.ozz.atlas.supply.purchaseorder.controller;

import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import com.ozz.atlas.supply.purchaseorder.dtos.*;
import com.ozz.atlas.supply.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<?> createPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.createPurchaseOrder(
                        buyerOrganizationPublicId,
                        createdByUserPublicId,
                        request
                ));
    }

    // 공급사 기준 조회 /api/supply/purchase-order?viewType=SUPPLIER
    // 구매사 기준 조회 /api/supply/purchase-order?viewType=BUYER
    @GetMapping
    public ResponseEntity<Page<PurchaseOrderSummaryResponse>> getPurchaseOrderList(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestParam("viewType") PurchaseOrderViewType viewType,
            @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.getPurchaseOrderList(
                        organizationPublicId,
                        viewType,
                        supplierPublicId,
                        pageable
                )
        );
    }


    @GetMapping("/{poPublicId}")
    public ResponseEntity<?> getPurchaseOrder(@PathVariable String poPublicId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrder(poPublicId));
    }

    @PatchMapping("/{poPublicId}")
    public ResponseEntity<?> updatePurchaseOrder(
                @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
                @PathVariable String poPublicId,
                @Valid @RequestBody UpdatePurchaseOrderRequest request
    ) {
            return ResponseEntity.ok(
                    purchaseOrderService.updatePurchaseOrder(buyerOrganizationPublicId, poPublicId, request)
            );
    }

    @DeleteMapping("/{poPublicId}")
    public ResponseEntity<?> deletePurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId
    ) {
        purchaseOrderService.deletePurchaseOrder(buyerOrganizationPublicId, poPublicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{poPublicId}/items")
    public ResponseEntity<?> addPurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @Valid @RequestBody CreatePurchaseOrderItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.addPurchaseOrderItem(buyerOrganizationPublicId, poPublicId, request));
    }

    @PatchMapping("/{poPublicId}/items/{poItemPublicId}")
    public ResponseEntity<?> updatePurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @PathVariable String poItemPublicId,
            @Valid @RequestBody UpdatePurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.updatePurchaseOrderItem(
                        buyerOrganizationPublicId,
                        poPublicId,
                        poItemPublicId,
                        request
                )
        );
    }

    @DeleteMapping("/{poPublicId}/items/{poItemPublicId}")
    public ResponseEntity<?> deletePurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @PathVariable String poItemPublicId
    ) {
        purchaseOrderService.deletePurchaseOrderItem(buyerOrganizationPublicId, poPublicId, poItemPublicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{poPublicId}/accept")
    public ResponseEntity<?> acceptPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String supplierOrganizationPublicId,
            @PathVariable String poPublicId
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.acceptPurchaseOrder(supplierOrganizationPublicId, poPublicId)
        );
    }

    @PostMapping("/{poPublicId}/reject")
    public ResponseEntity<?> rejectPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String supplierOrganizationPublicId,
            @PathVariable String poPublicId
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.rejectPurchaseOrder(supplierOrganizationPublicId, poPublicId)
        );
    }

    @PatchMapping("/{poPublicId}/items/{poItemPublicId}/confirm")
    public ResponseEntity<?> confirmPurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String supplierOrganizationPublicId,
            @PathVariable String poPublicId,
            @PathVariable String poItemPublicId,
            @Valid @RequestBody ConfirmPurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.confirmPurchaseOrderItem(
                        supplierOrganizationPublicId,
                        poPublicId,
                        poItemPublicId,
                        request
                )
        );
    }

    @PatchMapping("/{poPublicId}/status")
    public ResponseEntity<?> changePurchaseOrderStatus(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @Valid @RequestBody ChangePurchaseOrderStatusRequest request
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.changePurchaseOrderStatus(buyerOrganizationPublicId, poPublicId, request)
        );
    }
}
