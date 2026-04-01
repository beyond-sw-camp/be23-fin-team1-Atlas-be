package com.ozz.atlas.supply.supplier.repository;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<SupplySupplier, Long> {
}
