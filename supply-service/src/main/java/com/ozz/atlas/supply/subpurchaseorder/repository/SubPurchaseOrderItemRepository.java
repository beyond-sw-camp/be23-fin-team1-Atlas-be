package com.ozz.atlas.supply.subpurchaseorder.repository;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

public interface SubPurchaseOrderItemRepository extends JpaRepository<SupplySubPurchaseOrderItem, Long> {

    @Query("""
            select case when count(item) > 0 then true else false end
            from SupplySubPurchaseOrderItem item
            where item.subPurchaseOrder.parentPurchaseOrder.id = :poId
              and item.lineStatus in :activeStatuses
            """)
    boolean existsActiveByParentPurchaseOrderIdAndLineStatusIn(
            @Param("poId") Long poId,
            @Param("activeStatuses") Collection<SubPurchaseOrderLineStatus> activeStatuses
    );

    @Query("""
            select case when count(item) > 0 then true else false end
            from SupplySubPurchaseOrderItem item
            where item.parentPurchaseOrderItem.poItemId = :poItemId
              and item.lineStatus in :activeStatuses
            """)
    boolean existsActiveByParentPurchaseOrderItemIdAndLineStatusIn(
            @Param("poItemId") Long poItemId,
            @Param("activeStatuses") Collection<SubPurchaseOrderLineStatus> activeStatuses
    );

    // 부모 발주 상세 엔티티를 이미 찾은 뒤에는 publicId가 아니라 내부 PK로 합계를 계산한다.
    @Query("""
            select coalesce(sum(item.orderedQty), 0)
            from SupplySubPurchaseOrderItem item
            where item.parentPurchaseOrderItem.poItemId = :poItemId
              and item.lineStatus in :activeStatuses
            """)
    Long sumOrderedQtyByParentPurchaseOrderItemIdAndLineStatusIn(
            @Param("poItemId") Long poItemId,
            @Param("activeStatuses") Collection<SubPurchaseOrderLineStatus> activeStatuses
    );

    // 협력사와 품목은 둘 다 supply-service 내부 FK 엔티티이므로 내부 집계는 id로 처리한다.
    @Query("""
            select coalesce(sum(item.orderedQty), 0)
            from SupplySubPurchaseOrderItem item
            where item.subPurchaseOrder.supplier.id = :supplierId
              and item.item.id = :itemId
              and item.lineStatus in :activeStatuses
            """)
    Long sumOrderedQtyBySupplierIdAndItemIdAndLineStatusIn(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("activeStatuses") Collection<SubPurchaseOrderLineStatus> activeStatuses
    );

    @Query("""
            select coalesce(sum(item.orderedQty), 0)
            from SupplySubPurchaseOrderItem item
            where item.subPurchaseOrder.supplier.id = :supplierId
              and item.item.id = :itemId
              and item.subPurchaseOrder.orderedAt >= :monthStart
              and item.subPurchaseOrder.orderedAt < :nextMonthStart
              and item.lineStatus in :activeStatuses
            """)
    Long sumMonthlyOrderedQtyBySupplierIdAndItemIdAndOrderedAtBetweenAndLineStatusIn(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("nextMonthStart") LocalDateTime nextMonthStart,
            @Param("activeStatuses") Collection<SubPurchaseOrderLineStatus> activeStatuses
    );
}
