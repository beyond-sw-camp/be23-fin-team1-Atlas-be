package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
