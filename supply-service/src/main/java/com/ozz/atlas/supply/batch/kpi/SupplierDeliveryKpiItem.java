package com.ozz.atlas.supply.batch.kpi;

import java.time.LocalDate;

// 출하 1건
public record SupplierDeliveryKpiItem(
        LocalDate kpiDate,
        Long supplierId,
        String supplierPublicId,
        String supplierCode,
        String supplierName,
        boolean onTime,
        long delayMinutes
) {
}
