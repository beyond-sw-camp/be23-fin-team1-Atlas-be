package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {

    List<ShipmentStatusHistory> findByShipmentIdOrderByRecordedAtAsc(Long shipmentId);
}
