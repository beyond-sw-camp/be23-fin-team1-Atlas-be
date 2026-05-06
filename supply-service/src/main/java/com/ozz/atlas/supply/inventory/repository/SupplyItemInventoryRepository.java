package com.ozz.atlas.supply.inventory.repository;

import com.ozz.atlas.supply.inventory.domain.InventoryStatus;
import com.ozz.atlas.supply.inventory.domain.SupplyItemInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupplyItemInventoryRepository extends JpaRepository<SupplyItemInventory, Long> {

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    Optional<SupplyItemInventory> findByPublicIdAndStatusNot(String publicId, InventoryStatus status);

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    List<SupplyItemInventory> findAllBySupplier_OrganizationPublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
            String organizationPublicId,
            InventoryStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select inv
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.status in :statuses
          and inv.expirationDate >= :today
          and inv.remainingQty > inv.reservedQty
          and inv.logisticsNode.active = true
        order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
    """)
    List<SupplyItemInventory> findReservableForUpdate(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("statuses") Collection<InventoryStatus> statuses,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select inv
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.logisticsNode.id = :logisticsNodeId
          and inv.status in :statuses
          and inv.expirationDate >= :today
          and inv.remainingQty > inv.reservedQty
          and inv.logisticsNode.active = true
        order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
    """)
    List<SupplyItemInventory> findReservableForUpdateByNode(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("logisticsNodeId") Long logisticsNodeId,
            @Param("statuses") Collection<InventoryStatus> statuses,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select inv
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.status = com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          and inv.reservedQty > 0
          and inv.expirationDate >= :today
          and inv.logisticsNode.active = true
        order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
    """)
    List<SupplyItemInventory> findReservedForUpdate(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select inv
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.logisticsNode.id = :logisticsNodeId
          and inv.status = com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          and inv.reservedQty > 0
          and inv.expirationDate >= :today
          and inv.logisticsNode.active = true
        order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
    """)
    List<SupplyItemInventory> findReservedForUpdateByNode(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("logisticsNodeId") Long logisticsNodeId,
            @Param("today") LocalDate today
    );


    @Query("""
        select coalesce(sum(inv.remainingQty - inv.reservedQty), 0)
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.status in (
            com.ozz.atlas.supply.inventory.domain.InventoryStatus.ACTIVE,
            com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          )
          and inv.logisticsNode.active = true
          and inv.expirationDate >= :today
    """)
    Long sumAvailableQty(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("today") LocalDate today
    );

    @Query("""
        select coalesce(sum(inv.remainingQty - inv.reservedQty), 0)
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.logisticsNode.id = :logisticsNodeId
          and inv.status in (
            com.ozz.atlas.supply.inventory.domain.InventoryStatus.ACTIVE,
            com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          )
          and inv.logisticsNode.active = true
          and inv.expirationDate >= :today
    """)
    Long sumAvailableQtyByNode(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("logisticsNodeId") Long logisticsNodeId,
            @Param("today") LocalDate today
    );

    @Query("""
        select coalesce(sum(inv.reservedQty), 0)
        from SupplyItemInventory inv
        where inv.supplier.id = :supplierId
          and inv.item.id = :itemId
          and inv.logisticsNode.id = :logisticsNodeId
          and inv.status = com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          and inv.logisticsNode.active = true
          and inv.expirationDate >= :today
    """)
    Long sumReservedQtyByNode(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("logisticsNodeId") Long logisticsNodeId,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select inv
        from SupplyItemInventory inv
        where inv.inventoryId = :inventoryId
    """)
    Optional<SupplyItemInventory> findByInventoryIdForUpdate(@Param("inventoryId") Long inventoryId);

    @Query("""
        select count(inv)
        from SupplyItemInventory inv
        where inv.supplier.organizationPublicId = :organizationPublicId
          and inv.status in :statuses
    """)
    long countAttentionInventoryByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<InventoryStatus> statuses
    );

    @Query("""
        select inv.publicId
        from SupplyItemInventory inv
        where inv.supplier.organizationPublicId = :organizationPublicId
          and inv.status in :statuses
    """)
    List<String> findAttentionInventoryPublicIdsByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") Collection<InventoryStatus> statuses
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select inv
    from SupplyItemInventory inv
    where inv.supplier.id = :supplierId
      and inv.item.id = :itemId
      and inv.status in :statuses
      and inv.expirationDate >= :today
      and inv.remainingQty > 0
      and inv.logisticsNode.active = true
    order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
""")
    List<SupplyItemInventory> findAvailableForDeductUpdate(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("statuses") Collection<InventoryStatus> statuses,
            @Param("today") LocalDate today
    );

    @Query("""
    select coalesce(sum(inv.remainingQty), 0)
    from SupplyItemInventory inv
    where inv.supplier.organizationPublicId = :organizationPublicId
      and inv.status <> com.ozz.atlas.supply.inventory.domain.InventoryStatus.DELETED
""")
    Long sumRemainingQtyByOrganizationPublicId(@Param("organizationPublicId") String organizationPublicId);

    @Query("""
    select coalesce(sum(inv.reservedQty), 0)
    from SupplyItemInventory inv
    where inv.supplier.organizationPublicId = :organizationPublicId
      and inv.status <> com.ozz.atlas.supply.inventory.domain.InventoryStatus.DELETED
""")
    Long sumReservedQtyByOrganizationPublicId(@Param("organizationPublicId") String organizationPublicId);

    @Query("""
    select coalesce(sum(inv.remainingQty - inv.reservedQty), 0)
    from SupplyItemInventory inv
    where inv.supplier.organizationPublicId = :organizationPublicId
      and inv.status in (
        com.ozz.atlas.supply.inventory.domain.InventoryStatus.ACTIVE,
        com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
      )
      and inv.expirationDate >= :today
      and inv.logisticsNode.active = true
""")
    Long sumAvailableQtyByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("today") LocalDate today
    );

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})    List<SupplyItemInventory> findAllBySupplier_OrganizationPublicIdAndItem_PublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
            String organizationPublicId,
            String itemPublicId,
            InventoryStatus status
    );

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    List<SupplyItemInventory> findAllBySupplier_OrganizationPublicIdAndLogisticsNode_PublicIdAndStatusNotOrderByExpirationDateAscManufacturedDateAscInventoryIdAsc(
            String organizationPublicId,
            String logisticsNodePublicId,
            InventoryStatus status
    );

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    List<SupplyItemInventory> findAllBySupplier_IdAndItem_IdAndStatusNotOrderByExpirationDateDesc(
            Long supplierId,
            Long itemId,
            InventoryStatus status
    );

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    List<SupplyItemInventory> findAllBySupplier_IdAndItem_IdAndStatusNotOrderByExpirationDateAsc(
            Long supplierId,
            Long itemId,
            InventoryStatus status
    );

    @EntityGraph(attributePaths = {"supplier", "item", "logisticsNode"})
    List<SupplyItemInventory> findTop5BySupplier_OrganizationPublicIdAndLogisticsNode_PublicIdAndStatusNotOrderByCreatedAtDescInventoryIdDesc(
            String organizationPublicId,
            String logisticsNodePublicId,
            InventoryStatus status
    );

    @Query("""
    select count(inv) > 0
    from SupplyItemInventory inv
    where inv.logisticsNode.publicId = :nodePublicId
      and inv.logisticsNode.organizationPublicId = :organizationPublicId
      and inv.status not in (
        com.ozz.atlas.supply.inventory.domain.InventoryStatus.DELETED,
        com.ozz.atlas.supply.inventory.domain.InventoryStatus.EXHAUSTED
      )
      and inv.remainingQty > 0
""")
    boolean existsLiveInventoryInNode(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("nodePublicId") String nodePublicId
    );


}
