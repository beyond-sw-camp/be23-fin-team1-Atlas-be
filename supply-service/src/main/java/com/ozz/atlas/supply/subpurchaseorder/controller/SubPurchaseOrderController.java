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

    // 부모 발주를 실제로 수신한 협력사가 자기 하위 협력사에게 다시 발행하는 API다.
    @PostMapping
    public ResponseEntity<?> createSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String issuerOrganizationPublicId,
            @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
            @Valid @RequestBody CreateSubPurchaseOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subPurchaseOrderService.createSubPurchaseOrder(
                        issuerOrganizationPublicId,
                        createdByUserPublicId,
                        request
                ));
    }

    @GetMapping
    public ResponseEntity<?> getSubPurchaseOrdersByParentPo(
            @RequestHeader("X-Organization-Public-Id") String issuerOrganizationPublicId,
            @RequestParam String parentPoPublicId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getSubPurchaseOrdersByParentPo(
                        issuerOrganizationPublicId,
                        parentPoPublicId,
                        pageable
                )
        );
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedSubPurchaseOrders(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getReceivedSubPurchaseOrders(receiverOrganizationPublicId, pageable)
        );
    }

    @GetMapping("/{subPoPublicId}")
    public ResponseEntity<?> getSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PathVariable String subPoPublicId
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.getSubPurchaseOrder(organizationPublicId, subPoPublicId)
        );
    }

    @PostMapping("/{subPoPublicId}/accept")
    public ResponseEntity<?> acceptSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @PathVariable String subPoPublicId
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.acceptSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId)
        );
    }

    @PostMapping("/{subPoPublicId}/reject")
    public ResponseEntity<?> rejectSubPurchaseOrder(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @PathVariable String subPoPublicId
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.rejectSubPurchaseOrder(receiverOrganizationPublicId, subPoPublicId)
        );
    }

    // sub_po_item에 public_id가 없으므로 부모 발주 상세 publicId를 line path key로 쓴다.
    @PatchMapping("/{subPoPublicId}/items/{poItemPublicId}/confirm")
    public ResponseEntity<?> confirmSubPurchaseOrderItem(
            @RequestHeader("X-Organization-Public-Id") String receiverOrganizationPublicId,
            @PathVariable String subPoPublicId,
            @PathVariable String poItemPublicId,
            @Valid @RequestBody ConfirmSubPurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(
                subPurchaseOrderService.confirmSubPurchaseOrderItem(
                        receiverOrganizationPublicId,
                        subPoPublicId,
                        poItemPublicId,
                        request
                )
        );
    }
}
