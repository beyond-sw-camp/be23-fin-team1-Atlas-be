package com.ozz.atlas.supply.purchaseorder.service;

import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.purchaseorder.dtos.CreatePurchaseOrderRequest;
import com.ozz.atlas.supply.purchaseorder.dtos.PurchaseOrderDetailResponse;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;

//    public PurchaseOrderDetailResponse createPurchaseOrder(String buyerOrganizationPublicId, String createByUserPublicId, CreatePurchaseOrderRequest request) {
//
//    }
}
