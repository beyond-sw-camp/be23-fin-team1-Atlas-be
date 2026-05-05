package com.ozz.atlas.supply.subpurchaseorder.repository;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
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

    boolean existsBySubPoNumber(String subPoNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SupplySubPurchaseOrder> findTopBySubPoNumberStartingWithOrderBySubPoNumberDesc(String prefix);

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    @Query("""
        select subPo
        from SupplySubPurchaseOrder subPo
        where subPo.subPoStatus <> :deletedStatus
          and (
                (subPo.parentPurchaseOrder.supplier.id = :loginSupplierId and subPo.supplier.id in :relatedSupplierIds)
                or
                (subPo.parentPurchaseOrder.supplier.id in :relatedSupplierIds and subPo.supplier.id = :loginSupplierId)
          )
        order by subPo.orderedAt desc
        """)
    List<SupplySubPurchaseOrder> findAllBetweenSupplierAndRelatedSuppliers(
            @Param("loginSupplierId") Long loginSupplierId,
            @Param("relatedSupplierIds") Collection<Long> relatedSupplierIds,
            @Param("deletedStatus") SubPoStatus deletedStatus
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    @Query("""
        select subPo
        from SupplySubPurchaseOrder subPo
        where subPo.subPoStatus <> :deletedStatus
          and (
                (subPo.parentPurchaseOrder.supplier.id = :firstSupplierId and subPo.supplier.id = :secondSupplierId)
                or
                (subPo.parentPurchaseOrder.supplier.id = :secondSupplierId and subPo.supplier.id = :firstSupplierId)
          )
        order by subPo.orderedAt desc
        """)
    List<SupplySubPurchaseOrder> findAllBetweenSuppliers(
            @Param("firstSupplierId") Long firstSupplierId,
            @Param("secondSupplierId") Long secondSupplierId,
            @Param("deletedStatus") SubPoStatus deletedStatus
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllByParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
            String issuerOrganizationPublicId,
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    long countByParentPurchaseOrder_Supplier_OrganizationPublicIdAndSubPoStatusNot(
            String issuerOrganizationPublicId,
            SubPoStatus subPoStatus
    );

    @Query("""
    select count(subPo)
    from SupplySubPurchaseOrder subPo
    where subPo.subPoStatus in :statuses
      and (
          subPo.parentPurchaseOrder.buyerOrganizationPublicId = :organizationPublicId
          or subPo.parentPurchaseOrder.supplier.organizationPublicId = :organizationPublicId
          or subPo.supplier.organizationPublicId = :organizationPublicId
      )
    """)
    long countReadableByOrganizationPublicIdAndStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<SubPoStatus> statuses
    );

    @Query("""
    select subPo.publicId
    from SupplySubPurchaseOrder subPo
    where subPo.subPoStatus in :statuses
      and (
          subPo.parentPurchaseOrder.buyerOrganizationPublicId = :organizationPublicId
          or subPo.parentPurchaseOrder.supplier.organizationPublicId = :organizationPublicId
          or subPo.supplier.organizationPublicId = :organizationPublicId
      )
    """)
    List<String> findReadablePublicIdsByOrganizationPublicIdAndStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<SubPoStatus> statuses
    );

    @Query("""
    select coalesce(sum(subPo.totalAmount), 0)
    from SupplySubPurchaseOrder subPo
    where subPo.parentPurchaseOrder.supplier.organizationPublicId = :issuerOrganizationPublicId
      and subPo.subPoStatus <> :deletedStatus
    """)
    BigDecimal sumIssuedAmountBySupplierOrganizationPublicIdAndSubPoStatusNot(
            @Param("issuerOrganizationPublicId") String issuerOrganizationPublicId,
            @Param("deletedStatus") SubPoStatus deletedStatus
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    Page<SupplySubPurchaseOrder> findAllByParentPurchaseOrder_PublicIdAndParentPurchaseOrder_BuyerOrganizationPublicIdAndSubPoStatusNot(
            String parentPoPublicId,
            String buyerOrganizationPublicId,
            SubPoStatus subPoStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier",
            "subPurchaseOrderItems", "subPurchaseOrderItems.item", "subPurchaseOrderItems.parentPurchaseOrderItem"
    })
    Optional<SupplySubPurchaseOrder> findByPublicIdAndParentPurchaseOrder_BuyerOrganizationPublicIdAndSubPoStatusNot(
            String publicId,
            String buyerOrganizationPublicId,
            SubPoStatus subPoStatus
    );

    @EntityGraph(attributePaths = {"parentPurchaseOrder", "parentPurchaseOrder.supplier", "supplier"})
    List<SupplySubPurchaseOrder> findAllByParentPurchaseOrder_BuyerOrganizationPublicIdAndParentPurchaseOrder_Supplier_IdAndSubPoStatusNot(
            String buyerOrganizationPublicId,
            Long issuerSupplierId,
            SubPoStatus subPoStatus
    );


}
