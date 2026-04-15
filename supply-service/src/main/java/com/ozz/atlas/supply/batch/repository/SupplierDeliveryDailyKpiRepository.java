package com.ozz.atlas.supply.batch.repository;

import com.ozz.atlas.supply.batch.domain.SupplierDeliveryDailyKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface SupplierDeliveryDailyKpiRepository extends JpaRepository<SupplierDeliveryDailyKpi, Long> {

    // 해당 날짜, 해당 협력사 KPI 있는지
    Optional<SupplierDeliveryDailyKpi> findByKpiDateAndSupplierId(LocalDate kpiDate, Long supplierId);

    // 특정 날짜 KPI를 전부 삭제
    @Modifying
    @Query("delete from SupplierDeliveryDailyKpi k where k.kpiDate = :kpiDate")
    void deleteByKpiDate(@Param("kpiDate") LocalDate kpiDate);

}
