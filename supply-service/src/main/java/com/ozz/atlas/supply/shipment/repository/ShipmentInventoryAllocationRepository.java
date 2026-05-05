package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.ShipmentInventoryAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShipmentInventoryAllocationRepository extends JpaRepository<ShipmentInventoryAllocation, Long> {

    List<ShipmentInventoryAllocation> findByShipmentLineIdIn(Collection<Long> shipmentLineIds);
}
