package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByPublicId(String publicId);

    List<Shipment> findAllBySubPoIdIn(Collection<Long> subPoIds);

    Page<Shipment> findByOriginNodeIdInOrDestinationNodeIdIn(
            Collection<Long> originNodeIds,
            Collection<Long> destinationNodeIds,
            Pageable pageable
    );
    Optional<Shipment> findTopByShipmentNumberStartingWithOrderByShipmentNumberDesc(String prefix);

    boolean existsByShipmentNumber(String shipmentNumber);

    List<Shipment> findByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdInOrderByIdDesc(
            Collection<ShipmentStatus> originStatuses,
            Collection<Long> originNodeIds,
            Collection<ShipmentStatus> destinationStatuses,
            Collection<Long> destinationNodeIds
    );

    long countByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdIn(
            Collection<ShipmentStatus> originStatuses,
            Collection<Long> originNodeIds,
            Collection<ShipmentStatus> destinationStatuses,
            Collection<Long> destinationNodeIds
    );


}
