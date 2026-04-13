package com.ozz.atlas.supply.shipment.lotmapping.repository;

import com.ozz.atlas.supply.shipment.lotmapping.domain.ShipmentLotMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ShipmentLotMappingRepository extends JpaRepository<ShipmentLotMapping, Long> {

//    shipment 기준 목록 조회
    List<ShipmentLotMapping> findAllByShipment_IdOrderByIdDesc(Long shipmentId);

//    shipment-lot 중복 방지
    boolean existsByShipment_IdAndLot_Id(Long shipmentId, Long lotId);

//    lot 누적 출하 누적량 계산
    @Query("""
            select coalesce(sum(m.shippedQty), 0)
            from ShipmentLotMapping m
            where m.lot.id = :lotId
            """)
    BigDecimal sumShippedQtyByLotId(Long lotId);
}
