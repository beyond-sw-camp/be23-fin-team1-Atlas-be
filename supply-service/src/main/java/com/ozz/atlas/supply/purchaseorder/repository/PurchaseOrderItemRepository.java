package com.ozz.atlas.supply.purchaseorder.repository;

import com.ozz.atlas.supply.item.dtos.ItemLinkedPurchaseOrderResponse;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<SupplyPurchaseOrderItem, Long> {
    boolean existsByPublicId(String publicId);
    Optional<SupplyPurchaseOrderItem> findByPublicId(String publicId);

    @Query("""
        select count(distinct poi.item.id)
        from SupplyPurchaseOrderItem poi
        join poi.purchaseOrder po
        where poi.item.supplier.organizationPublicId = :organizationPublicId
          and po.poStatus <> com.ozz.atlas.supply.purchaseorder.domain.PoStatus.DELETED
          and poi.itemStatus <> com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus.DELETED
          and po.orderedAt >= :from
          and po.orderedAt < :to
    """)
    long countDistinctOrderedItemsToday(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select new com.ozz.atlas.supply.item.dtos.ItemLinkedPurchaseOrderResponse(
            po.publicId,
            po.poNumber,
            po.buyerOrganizationPublicId,
            po.poStatus,
            po.orderedAt,
            poi.publicId,
            poi.orderedQty,
            poi.confirmedQty,
            poi.itemStatus,
            poi.expectedDueDate
        )
        from SupplyPurchaseOrderItem poi
        join poi.purchaseOrder po
        where poi.item.publicId = :itemPublicId
          and po.poStatus <> com.ozz.atlas.supply.purchaseorder.domain.PoStatus.DELETED
          and poi.itemStatus <> com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus.DELETED
        order by po.orderedAt desc
    """)
    List<ItemLinkedPurchaseOrderResponse> findLinkedOrdersByItemPublicId(@Param("itemPublicId") String itemPublicId);
}