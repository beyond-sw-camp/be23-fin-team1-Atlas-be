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
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.search.dtos.PurchaseOrderSearchDto;
import com.ozz.atlas.supply.purchaseorder.search.service.PurchaseOrderSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/purchase-order")
@Tag(name = "PurchaseOrder")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderSearchService purchaseOrderSearchService;

    @Operation(summary = "발주 생성")
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
    @Operation(summary = "발주 목록 조회")
    @GetMapping
    public ResponseEntity<Page<PurchaseOrderSummaryResponse>> getPurchaseOrderList(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestParam("viewType") PurchaseOrderViewType viewType,
            @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "poStatus", required = false) PoStatus poStatus,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PurchaseOrderSearchDto searchDto = PurchaseOrderSearchDto.builder()
                .organizationPublicId(organizationPublicId)
                .viewType(viewType)
                .supplierPublicId(supplierPublicId)
                .keyword(keyword)
                .poStatus(poStatus)
                .build();

        if (purchaseOrderSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(purchaseOrderSearchService.search(pageable, searchDto));
        }
        return ResponseEntity.ok(
                purchaseOrderService.getPurchaseOrderList(
                        organizationPublicId,
                        viewType,
                        supplierPublicId,
                        pageable
                )
        );
    }


    @Operation(summary = "발주 상세 조회")
    @GetMapping("/{poPublicId}")
    public ResponseEntity<?> getPurchaseOrder(@PathVariable String poPublicId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrder(poPublicId));
    }

    @Operation(summary = "발주 수정")
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

    @Operation(summary = "발주 삭제")
    @DeleteMapping("/{poPublicId}")
    public ResponseEntity<?> deletePurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId
    ) {
        purchaseOrderService.deletePurchaseOrder(buyerOrganizationPublicId, poPublicId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "발주 품목 추가")
    @PostMapping("/{poPublicId}/items")
    public ResponseEntity<?> addPurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @Valid @RequestBody CreatePurchaseOrderItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.addPurchaseOrderItem(buyerOrganizationPublicId, poPublicId, request));
    }

    @Operation(summary = "발주 품목 수정")
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

    @Operation(summary = "발주 품목 삭제")
    @DeleteMapping("/{poPublicId}/items/{poItemPublicId}")
    public ResponseEntity<?> deletePurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @PathVariable String poPublicId,
            @PathVariable String poItemPublicId
    ) {
        purchaseOrderService.deletePurchaseOrderItem(buyerOrganizationPublicId, poPublicId, poItemPublicId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "발주 거절")
    @PostMapping("/{poPublicId}/reject")
    public ResponseEntity<?> rejectPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String supplierOrganizationPublicId,
            @PathVariable String poPublicId
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.rejectPurchaseOrder(supplierOrganizationPublicId, poPublicId)
        );
    }

    @Operation(summary = "발주 품목 확인")
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

    @Operation(summary = "발주 상태 변경")
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

    @Operation(summary = "발주 일괄 생성")
    @PostMapping("/batch")
    public ResponseEntity<?> createPurchaseOrdersBatch(
            @RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
            @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
            @Valid @RequestBody CreatePurchaseOrderBatchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.createPurchaseOrdersBatch(
                        buyerOrganizationPublicId,
                        createdByUserPublicId,
                        request
                ));
    }

    @Operation(summary = "발주 대시보드 요약 조회")
    @GetMapping("/dashboard")
    public ResponseEntity<OrderDashboardSummaryResponse> getOrderDashboardSummary(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.getOrderDashboardSummary(
                        organizationPublicId,
                        organizationType
                )
        );
    }

}
