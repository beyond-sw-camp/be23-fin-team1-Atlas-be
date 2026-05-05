package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShipmentLineRepository extends JpaRepository<ShipmentLine, Long> {

    List<ShipmentLine> findByShipmentIdOrderByIdAsc(Long shipmentId);

    List<ShipmentLine> findByShipmentIdIn(Collection<Long> shipmentIds);
}
