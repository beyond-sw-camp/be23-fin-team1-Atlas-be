package com.ozz.atlas.supply.subpurchaseorder.repository;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
