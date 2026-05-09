package com.ozz.atlas.supply.purchaseorder.repository;

import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderHistoryRepository extends JpaRepository<PurchaseOrderHistory, Long> {

    List<PurchaseOrderHistory> findByPurchaseOrderIdOrderByRecordedAtDesc(Long purchaseOrderId);
}
