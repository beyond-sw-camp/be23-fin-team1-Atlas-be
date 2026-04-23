package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByPublicId(String publicId);

    List<Shipment> findAllBySubPoIdIn(Collection<Long> subPoIds);

}
