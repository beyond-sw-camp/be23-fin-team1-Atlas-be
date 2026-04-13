package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.EtaProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtaProjectionRepository extends JpaRepository<EtaProjection, Long> {

    List<EtaProjection> findByShipmentIdOrderByCalculatedAtDesc(Long shipmentId);
}
