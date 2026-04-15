package com.ozz.atlas.supply.batch.config;

import com.ozz.atlas.supply.batch.service.SupplyBatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyBatchScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SupplyBatchJobService supplyBatchJobService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void runCertificateExpiryWarningJob() {
        try {
            supplyBatchJobService.runCertificateExpiryWarning(LocalDate.now(KST));
        } catch (Exception e) {
            log.error("certificateExpiryWarningJob failed", e);
        }
    }

    @Scheduled(cron = "0 20 0 * * *", zone = "Asia/Seoul")
    public void runLotExpiryAggregationJob() {
        try {
            supplyBatchJobService.runLotExpiryAggregation(LocalDate.now(KST));
        } catch (Exception e) {
            log.error("lotExpiryAggregationJob failed", e);
        }
    }

    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul")
    public void runSupplierDeliveryKpiJob() {
        try {
            supplyBatchJobService.runSupplierDeliveryKpi(LocalDate.now(KST).minusDays(1));
        } catch (Exception e) {
            log.error("supplierDeliveryKpiJob failed", e);
        }
    }
}
