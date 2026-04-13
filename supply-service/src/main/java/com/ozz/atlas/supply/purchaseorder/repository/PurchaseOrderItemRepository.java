package com.ozz.atlas.supply.purchaseorder.repository;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<SupplyPurchaseOrderItem, Long> {
    boolean existsByPublicId(String publicId);
    Optional<SupplyPurchaseOrderItem> findByPublicId(String publicId);
}