package com.ozz.atlas.supply.purchaseorder.repository;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<SupplyPurchaseOrder, Long> {

    boolean existsByPoNumberAndBuyerOrganizationPublicId(String poNumber, String buyerOrganizationPublicId);

    @EntityGraph(attributePaths = {"supplier"})
    Page<SupplyPurchaseOrder> findAllByBuyerOrganizationPublicId(String buyerOrganizationPublicId, Pageable pageable);

    @EntityGraph(attributePaths = {"supplier", "purchaseOrderItems", "purchaseOrderItems.item"})
    Optional<SupplyPurchaseOrder> findByPublicIdAndBuyerOrganizationPublicId(
            String publicId,
            String buyerOrganizationPublicId
    );
}