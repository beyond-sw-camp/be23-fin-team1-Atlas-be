package com.ozz.atlas.supply.subpurchaseorder.repository;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface SubPurchaseOrderRepository extends JpaRepository<SupplySubPurchaseOrder, Long> {

    boolean existsBySubPoNumberAndParentPurchaseOrder_IdAndSubPoStatusNot(
            String subPoNumber,
            Long parentPurchaseOrderId,
            SubPoStatus subPoStatus
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllByParentPurchaseOrder_PublicIdAndSubPoStatusNot(
            String parentPoPublicId,
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllByParentPurchaseOrder_PublicIdAndParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
            String parentPoPublicId,
            String issuerOrganizationPublicId,
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllBySupplier_OrganizationPublicIdAndSubPoStatusNot(
            String receiverOrganizationPublicId,
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllBySubPoStatusNot(
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier",
            "subPurchaseOrderItems", "subPurchaseOrderItems.item", "subPurchaseOrderItems.parentPurchaseOrderItem"
    })
    Optional<SupplySubPurchaseOrder> findByPublicIdAndSubPoStatusNot(
            String publicId,
            SubPoStatus subPoStatus
    );

    @EntityGraph(attributePaths = {
            "parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier",
            "subPurchaseOrderItems", "subPurchaseOrderItems.item", "subPurchaseOrderItems.parentPurchaseOrderItem"
    })
    Optional<SupplySubPurchaseOrder> findByPublicIdAndParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
            String publicId,
            String issuerOrganizationPublicId,
            SubPoStatus subPoStatus
    );

    @EntityGraph(attributePaths = {
            "parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier",
            "subPurchaseOrderItems", "subPurchaseOrderItems.item", "subPurchaseOrderItems.parentPurchaseOrderItem"
    })
    Optional<SupplySubPurchaseOrder> findByPublicIdAndSupplier_OrganizationPublicIdAndSubPoStatusNot(
            String publicId,
            String receiverOrganizationPublicId,
            SubPoStatus subPoStatus
    );

    @Query("""
        select coalesce(sum(subPo.totalAmount), 0)
        from SupplySubPurchaseOrder subPo
        where subPo.supplier.id = :supplierId
          and subPo.subPoStatus not in :excludedStatuses
        """)
    BigDecimal sumReceivedAmountBySupplierId(
            @Param("supplierId") Long supplierId,
            @Param("excludedStatuses") Collection<SubPoStatus> excludedStatuses
    );
}
