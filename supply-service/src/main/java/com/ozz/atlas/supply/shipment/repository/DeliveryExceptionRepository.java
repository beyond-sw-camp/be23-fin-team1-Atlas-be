package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.DeliveryException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryExceptionRepository extends JpaRepository<DeliveryException, Long> {

    List<DeliveryException> findByShipmentIdOrderByDetectedAtDesc(Long shipmentId);
}
