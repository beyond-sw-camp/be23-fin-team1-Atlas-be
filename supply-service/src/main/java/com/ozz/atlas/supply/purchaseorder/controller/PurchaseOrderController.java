//package com.ozz.atlas.supply.purchaseorder.controller;
//
//import com.ozz.atlas.supply.purchaseorder.dtos.CreatePurchaseOrderRequest;
//import com.ozz.atlas.supply.purchaseorder.service.PurchaseOrderService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/purchase-order")
//public class PurchaseOrderController {
//
//    private final PurchaseOrderService purchaseOrderService;
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createPurchaseOrder(@RequestHeader("X-Organization-Public-Id") String buyerOrganizationPublicId,
//                                                 @RequestHeader("X-User-Public-Id") String createdByUserPublicId,
//                                                 @Valid @RequestBody CreatePurchaseOrderRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(purchaseOrderService.createPurchaseOrder(
//                        buyerOrganizationPublicId,
//                        createdByUserPublicId,
//                        request
//                ));
//    }
//
//}
