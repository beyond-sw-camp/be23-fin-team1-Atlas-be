package com.ozz.atlas.supply.supplier.capability.repository;

import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierItemCapabilityRepository extends JpaRepository<SupplySupplierItemCapability, Long> {

    boolean existsBySupplier_IdAndItem_Id(Long supplierId, Long itemId);

    @EntityGraph(attributePaths = {"supplier", "item"})
    Optional<SupplySupplierItemCapability> findBySupplier_IdAndItem_Id(
            Long supplierId,
            Long itemId
    );

    @EntityGraph(attributePaths = {"supplier", "item"})
    List<SupplySupplierItemCapability> findAllBySupplier_IdOrderByItem_ItemNameAsc(Long supplierId);
}
