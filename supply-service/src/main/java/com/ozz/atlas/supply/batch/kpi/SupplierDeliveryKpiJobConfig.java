package com.ozz.atlas.supply.batch.kpi;

import com.ozz.atlas.supply.batch.domain.SupplierDeliveryDailyKpi;
import com.ozz.atlas.supply.batch.repository.SupplierDeliveryDailyKpiRepository;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SupplierDeliveryKpiJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SupplierDeliveryDailyKpiRepository supplierDeliveryDailyKpiRepository;

    @Bean
    public Job supplierDeliveryKpiJob() {
        return new JobBuilder("supplierDeliveryKpiJob", jobRepository)
                .start(deleteSupplierDeliveryKpiStep())
                .next(supplierDeliveryKpiStep())
                .build();
    }

    @Bean
    public Step deleteSupplierDeliveryKpiStep() {
        return new StepBuilder("deleteSupplierDeliveryKpiStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String runDate = (String) chunkContext.getStepContext()
                            .getJobParameters()
                            .get("runDate");

                    supplierDeliveryDailyKpiRepository.deleteByKpiDate(LocalDate.parse(runDate));
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step supplierDeliveryKpiStep() {
        return new StepBuilder("supplierDeliveryKpiStep", jobRepository)
                .<Shipment, SupplierDeliveryKpiItem>chunk(100, transactionManager)
                .reader(supplierDeliveryKpiReader(null))
                .processor(supplierDeliveryKpiProcessor(null))
                .writer(supplierDeliveryKpiWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Shipment> supplierDeliveryKpiReader(
            @Value("#{jobParameters['runDate']}") String runDate
    ) {
        LocalDate date = LocalDate.parse(runDate);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Map<String, Object> params = new HashMap<>();
        params.put("from", from);
        params.put("to", to);

        return new JpaPagingItemReaderBuilder<Shipment>()
                .name("supplierDeliveryKpiReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("""
                        select s
                        from Shipment s
                        where s.actualArrivedAt >= :from
                          and s.actualArrivedAt < :to
                          and (s.poId is not null or s.subPoId is not null)
                        """)
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Shipment, SupplierDeliveryKpiItem> supplierDeliveryKpiProcessor(
            @Value("#{jobParameters['runDate']}") String runDate
    ) {
        return shipment -> {
            if (shipment.getActualArrivedAt() == null || shipment.getArrivalEta() == null) {
                return null;
            }

            SupplySupplier supplier = resolveTargetSupplier(shipment);
            if (supplier == null) {
                return null;
            }

            boolean onTime = !shipment.getActualArrivedAt().isAfter(shipment.getArrivalEta());
            long delayMinutes = onTime
                    ? 0L
                    : Duration.between(shipment.getArrivalEta(), shipment.getActualArrivedAt()).toMinutes();

            return new SupplierDeliveryKpiItem(
                    LocalDate.parse(runDate),
                    supplier.getId(),
                    supplier.getPublicId(),
                    supplier.getSupplierCode(),
                    supplier.getSupplierName(),
                    onTime,
                    delayMinutes
            );
        };
    }

    @Bean
    public ItemWriter<SupplierDeliveryKpiItem> supplierDeliveryKpiWriter() {
        return chunk -> {
            for (SupplierDeliveryKpiItem item : chunk.getItems()) {
                SupplierDeliveryDailyKpi kpi = supplierDeliveryDailyKpiRepository
                        .findByKpiDateAndSupplierId(item.kpiDate(), item.supplierId())
                        .orElseGet(() -> new SupplierDeliveryDailyKpi(
                                item.kpiDate(),
                                item.supplierId(),
                                item.supplierPublicId(),
                                item.supplierCode(),
                                item.supplierName()
                        ));

                kpi.accumulate(item.onTime(), item.delayMinutes());
                supplierDeliveryDailyKpiRepository.save(kpi);
            }
        };
    }

    private SupplySupplier resolveTargetSupplier(Shipment shipment) {
        if (shipment.getSubPoId() != null) {
            Optional<SupplySubPurchaseOrder> subPoOptional = subPurchaseOrderRepository.findById(shipment.getSubPoId());
            if (subPoOptional.isPresent()) {
                return subPoOptional.get().getSupplier();
            }
        }

        if (shipment.getPoId() != null) {
            Optional<SupplyPurchaseOrder> poOptional = purchaseOrderRepository.findById(shipment.getPoId());
            if (poOptional.isPresent()) {
                return poOptional.get().getSupplier();
            }
        }

        return null;
    }
}
