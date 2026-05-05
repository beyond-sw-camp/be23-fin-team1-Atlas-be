package com.ozz.atlas.supply.purchaseorder.repository;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
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

public interface PurchaseOrderRepository extends JpaRepository<SupplyPurchaseOrder, Long> {

    boolean existsByPoNumberAndBuyerOrganizationPublicIdAndPoStatusNot(
            String poNumber,
            String buyerOrganizationPublicId,
            PoStatus poStatus
    );

    boolean existsByPoNumberAndBuyerOrganizationPublicIdAndIdNotAndPoStatusNot(
            String poNumber,
            String buyerOrganizationPublicId,
            Long id,
            PoStatus poStatus
    );

    @EntityGraph(attributePaths = {"supplier"})
    Page<SupplyPurchaseOrder> findAllByBuyerOrganizationPublicIdAndPoStatusNot(
            String buyerOrganizationPublicId,
            PoStatus poStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"supplier"})
    Page<SupplyPurchaseOrder> findAllBySupplier_PublicIdAndPoStatusNot(
            String supplierPublicId,
            PoStatus poStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"supplier", "purchaseOrderItems", "purchaseOrderItems.item"})
    Optional<SupplyPurchaseOrder> findByPublicIdAndPoStatusNot(
            String publicId,
            PoStatus poStatus
    );

    @EntityGraph(attributePaths = {"supplier", "purchaseOrderItems", "purchaseOrderItems.item"})
    Optional<SupplyPurchaseOrder> findByPublicIdAndBuyerOrganizationPublicIdAndPoStatusNot(
            String publicId,
            String buyerOrganizationPublicId,
            PoStatus poStatus
    );

    @EntityGraph(attributePaths = {"supplier", "purchaseOrderItems", "purchaseOrderItems.item"})
    Optional<SupplyPurchaseOrder> findByPublicIdAndSupplier_OrganizationPublicIdAndPoStatusNot(
            String publicId,
            String supplierOrganizationPublicId,
            PoStatus poStatus
    );

    Page<SupplyPurchaseOrder> findAllBySupplier_OrganizationPublicIdAndPoStatusNot(
            String organizationPublicId,
            PoStatus poStatus,
            Pageable pageable
    );

    List<SupplyPurchaseOrder> findAllBySupplier_OrganizationPublicIdAndPoStatusNot(
            String organizationPublicId,
            PoStatus poStatus
    );

    Page<SupplyPurchaseOrder> findAllBySupplier_OrganizationPublicIdAndSupplier_PublicIdAndPoStatusNot(
            String organizationPublicId,
            String supplierPublicId,
            PoStatus poStatus,
            Pageable pageable
    );

    @Query("""
        select coalesce(sum(po.totalAmount), 0)
        from SupplyPurchaseOrder po
        where po.supplier.id = :supplierId
          and po.poStatus not in :excludedStatuses
        """)
    BigDecimal sumReceivedAmountBySupplierId(
            @Param("supplierId") Long supplierId,
            @Param("excludedStatuses") Collection<PoStatus> excludedStatuses
    );

    boolean existsByPoNumber(String poNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SupplyPurchaseOrder> findTopByPoNumberStartingWithOrderByPoNumberDesc(String prefix);

    boolean existsByBuyerOrganizationPublicIdAndSupplier_IdAndPoStatusNot(
            String buyerOrganizationPublicId,
            Long supplierId,
            PoStatus poStatus
    );

    @EntityGraph(attributePaths = {"supplier"})
    List<SupplyPurchaseOrder> findAllByBuyerOrganizationPublicIdAndPoStatusNot(
            String buyerOrganizationPublicId,
            PoStatus poStatus
    );

    long countByBuyerOrganizationPublicIdAndPoStatusNot(String buyerOrganizationPublicId, PoStatus poStatus);

    long countByBuyerOrganizationPublicIdAndPoStatus(String buyerOrganizationPublicId, PoStatus poStatus);

    @Query("""
    select coalesce(sum(po.totalAmount), 0)
    from SupplyPurchaseOrder po
    where po.buyerOrganizationPublicId = :buyerOrganizationPublicId
      and po.poStatus <> :deletedStatus
    """)
    BigDecimal sumAmountByBuyerOrganizationPublicIdAndPoStatusNot(
            @Param("buyerOrganizationPublicId") String buyerOrganizationPublicId,
            @Param("deletedStatus") PoStatus deletedStatus
    );

    long countBySupplier_OrganizationPublicIdAndPoStatusNot(String organizationPublicId, PoStatus poStatus);

    @Query("""
    select coalesce(sum(po.totalAmount), 0)
    from SupplyPurchaseOrder po
    where po.supplier.organizationPublicId = :organizationPublicId
      and po.poStatus <> :deletedStatus
    """)
    BigDecimal sumAmountBySupplierOrganizationPublicIdAndPoStatusNot(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("deletedStatus") PoStatus deletedStatus
    );

    @Query("""
    select count(po)
    from SupplyPurchaseOrder po
    where po.poStatus <> :deletedStatus
      and (
            po.buyerOrganizationPublicId = :organizationPublicId
            or po.supplier.organizationPublicId = :organizationPublicId
      )
    """)
    long countRelatedPurchaseOrders(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("deletedStatus") PoStatus deletedStatus
    );

    @Query("""
    select count(po)
    from SupplyPurchaseOrder po
    where po.poStatus in :statuses
      and (
            po.buyerOrganizationPublicId = :organizationPublicId
            or po.supplier.organizationPublicId = :organizationPublicId
      )
    """)
    long countRelatedPurchaseOrdersByStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<PoStatus> statuses
    );

    @Query("""
    select po.publicId
    from SupplyPurchaseOrder po
    where po.poStatus in :statuses
      and (
            po.buyerOrganizationPublicId = :organizationPublicId
            or po.supplier.organizationPublicId = :organizationPublicId
      )
    """)
    List<String> findRelatedPurchaseOrderPublicIdsByStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<PoStatus> statuses
    );


}
