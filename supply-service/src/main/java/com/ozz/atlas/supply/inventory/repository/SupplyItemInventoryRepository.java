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

    @EntityGraph(attributePaths = {"supplier", "item"})
    Optional<SupplyItemInventory> findByPublicIdAndStatusNot(String publicId, InventoryStatus status);

    @EntityGraph(attributePaths = {"supplier", "item"})
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
          and inv.status = com.ozz.atlas.supply.inventory.domain.InventoryStatus.RESERVED
          and inv.reservedQty > 0
        order by inv.expirationDate asc, inv.manufacturedDate asc, inv.inventoryId asc
    """)
    List<SupplyItemInventory> findReservedForUpdate(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId
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
          and inv.expirationDate >= :today
    """)
    Long sumAvailableQty(
            @Param("supplierId") Long supplierId,
            @Param("itemId") Long itemId,
            @Param("today") LocalDate today
    );

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
}
