package com.ozz.atlas.supply.purchaseorder.controller;

import com.ozz.atlas.supply.purchaseorder.dtos.ChangePurchaseOrderStatusRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.CreatePurchaseOrderRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.UpdatePurchaseOrderRequest;
import com.ozz.atlas.supply.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // 발주 생성
    @PostMapping("/create")
    public ResponseEntity<?> createPurchaseOrder(@RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
                                                 @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
                                                 @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.createPurchaseOrder(
                        buyerOrganizationPublicId,
                        createdByUserPublicId,
                        request
                ));
    }

    // 발주 목록 조회
    @GetMapping
    public ResponseEntity<?> getPurchaseOrderList(@RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
                                                  @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
                                                  @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                purchaseOrderService.getPurchaseOrderList(organizationPublicId, supplierPublicId, pageable)
        );
    }

    // 발주 단건 조회
    @GetMapping("/{poPublicId}")
    public ResponseEntity<?> getPurchaseOrder(@PathVariable String poPublicId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrder(poPublicId));
    }

    // 발주 수정
    @PatchMapping("/{poPublicId}")
    public ResponseEntity<?> updatePurchaseOrder(@RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
                                                 @PathVariable String poPublicId,
                                                 @Valid @RequestBody UpdatePurchaseOrderRequest request) {
        return ResponseEntity.ok(
                purchaseOrderService.updatePurchaseOrder(buyerOrganizationPublicId, poPublicId, request));
    }

    // 발주 상태 수정
    @PatchMapping("/{poPublicId}/status")
    public ResponseEntity<?> changePurchaseOrderStatus(@RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
                                                       @PathVariable String poPublicId,
                                                       @Valid @RequestBody ChangePurchaseOrderStatusRequest request) {
        return ResponseEntity.ok(
                purchaseOrderService.changePurchaseOrderStatus(buyerOrganizationPublicId, poPublicId, request)
        );
    }



}
