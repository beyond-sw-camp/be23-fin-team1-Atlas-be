package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.ShipmentCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentCheckpointRepository extends JpaRepository<ShipmentCheckpoint, Long> {

    List<ShipmentCheckpoint> findByShipmentIdOrderByActualAtAsc(Long shipmentId);
}
