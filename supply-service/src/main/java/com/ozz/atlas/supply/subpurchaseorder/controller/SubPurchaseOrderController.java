package com.ozz.atlas.supply.subpurchaseorder.controller;

import com.ozz.atlas.supply.subpurchaseorder.dtos.ConfirmSubPurchaseOrderItemRequest;
import com.ozz.atlas.supply.subpurchaseorder.dtos.CreateSubPurchaseOrderRequest;
import com.ozz.atlas.supply.subpurchaseorder.service.SubPurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/sub-purchase-orders")
public class SubPurchaseOrderController {

    private final SubPurchaseOrderService subPurchaseOrderService;

    @PostMapping
    public ResponseEntity<?> createSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String issuerOrganizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
            @Valid @RequestBody CreateSubPurchaseOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subPurchaseOrderService.createSubPurchaseOrder(
                        issuerOrganizationPublicId,
                        organizationType,
                        createdByUserPublicId,
                        request
                ));
    }

    @GetMapping
    public ResponseEntity<?> getSubPurchaseOrdersByParentPo(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam String parentPoPublicId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getSubPurchaseOrdersByParentPo(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        parentPoPublicId,
                        pageable
                )
        );
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedSubPurchaseOrders(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getReceivedSubPurchaseOrders(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        pageable
                )
        );
    }

    @GetMapping("/{subPoPublicId}")
    public ResponseEntity<?> getSubPurchaseOrder(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String subPoPublicId
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getSubPurchaseOrder(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        subPoPublicId
                )
        );
    }

    @PostMapping("/{subPoPublicId}/reject")
    public ResponseEntity<?> rejectSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @PathVariable String subPoPublicId
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.rejectSubPurchaseOrder(
                        receiverOrganizationPublicId,
                        organizationType,
                        subPoPublicId
                )
        );
    }

    @PatchMapping("/{subPoPublicId}/items/{parentPoItemPublicId}/{itemPublicId}/confirm")
    public ResponseEntity<?> confirmSubPurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @PathVariable String subPoPublicId,
            @PathVariable String parentPoItemPublicId,
            @PathVariable String itemPublicId,
            @Valid @RequestBody ConfirmSubPurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.confirmSubPurchaseOrderItem(
                        receiverOrganizationPublicId,
                        organizationType,
                        subPoPublicId,
                        parentPoItemPublicId,
                        itemPublicId,
                        request
                )
        );
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentSubPurchaseOrders(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getSentSubPurchaseOrders(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        pageable
                )
        );
    }

}
