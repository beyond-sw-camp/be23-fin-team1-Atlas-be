package com.ozz.atlas.supply.shipment.repository;

import com.ozz.atlas.supply.shipment.domain.ShipmentLine;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShipmentLineRepository extends JpaRepository<ShipmentLine, Long> {

    List<ShipmentLine> findByShipmentIdOrderByIdAsc(Long shipmentId);

    List<ShipmentLine> findByShipmentIdIn(Collection<Long> shipmentIds);

    @Query("""
        select coalesce(sum(line.quantity), 0)
        from ShipmentLine line
        join Shipment shipment on shipment.id = line.shipmentId
        where line.sourceItemPublicId = :sourceItemPublicId
          and shipment.status in :statuses
    """)
    Long sumQuantityBySourceItemPublicIdAndShipmentStatusIn(
            @Param("sourceItemPublicId") String sourceItemPublicId,
            @Param("statuses") Collection<ShipmentStatus> statuses
    );
}
