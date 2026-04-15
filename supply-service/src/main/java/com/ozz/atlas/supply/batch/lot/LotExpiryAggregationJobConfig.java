package com.ozz.atlas.supply.batch.lot;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.batch.domain.LotExpiryAggregation;
import com.ozz.atlas.supply.batch.domain.LotExpiryBucket;
import com.ozz.atlas.supply.batch.repository.LotExpiryAggregationRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.domain.LotStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class LotExpiryAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final SupplyItemRepository supplyItemRepository;
    private final LotExpiryAggregationRepository lotExpiryAggregationRepository;

    @Bean
    public Job lotExpiryAggregationJob() {
        return new JobBuilder("lotExpiryAggregationJob", jobRepository)
                .start(deleteLotExpiryAggregationStep()) // 기존 집계 삭제
                .next(lotExpiryAggregationStep()) // 재집계
                .build();
    }

    // tasklet 방식 (짧은 작업 1번)
    @Bean
    public Step deleteLotExpiryAggregationStep() {
        return new StepBuilder("deleteLotExpiryAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String runDate = (String) chunkContext.getStepContext()
                            .getJobParameters()
                            .get("runDate");

                    lotExpiryAggregationRepository.deleteByAggregationDate(LocalDate.parse(runDate));
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step lotExpiryAggregationStep() {
        return new StepBuilder("lotExpiryAggregationStep", jobRepository)
                .<Lot, LotExpiryAggregationItem>chunk(200, transactionManager)
                .reader(lotExpiryAggregationReader())
                .processor(lotExpiryAggregationProcessor(null))
                .writer(lotExpiryAggregationWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Lot> lotExpiryAggregationReader() {
        Map<String, Object> params = new HashMap<>();
        params.put("discardedStatus", LotStatus.DISCARDED);

        return new JpaPagingItemReaderBuilder<Lot>()
                .name("lotExpiryAggregationReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(200)
                .queryString("""
                    select l
                    from Lot l
                    where l.lotStatus <> :discardedStatus
                    """) // 나중에 soft delete 제외, 이미 종료된 LOT 제외,  기간 종료된 LOT 제외, 기간 조건 추가
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Lot, LotExpiryAggregationItem> lotExpiryAggregationProcessor(
            @Value("#{jobParameters['runDate']}") String runDate
    ) {
        return lot -> {
            LocalDate aggregationDate = LocalDate.parse(runDate);
            LocalDate expiryDate = resolveExpiryDate(lot);

            // 유통기한 계산할 수 없으면 NO_EXPIRY
            if (expiryDate == null) {
                return new LotExpiryAggregationItem(
                        aggregationDate,
                        lot.getSupplierPublicId(),
                        lot.getItemPublicId(),
                        LotExpiryBucket.NO_EXPIRY,
                        lot.getQty()
                );
            }

            long daysRemaining = ChronoUnit.DAYS.between(aggregationDate, expiryDate);
            LotExpiryBucket bucket = LotExpiryBucket.fromDaysRemaining(daysRemaining);

            return new LotExpiryAggregationItem(
                    aggregationDate,
                    lot.getSupplierPublicId(),
                    lot.getItemPublicId(),
                    bucket,
                    lot.getQty()
            );
        };
    }

    @Bean
    public ItemWriter<LotExpiryAggregationItem> lotExpiryAggregationWriter() {
        return chunk -> {
            for (LotExpiryAggregationItem item : chunk.getItems()) {
                LotExpiryAggregation aggregation = lotExpiryAggregationRepository
                        .findByAggregationDateAndSupplierPublicIdAndItemPublicIdAndBucket(
                                item.aggregationDate(),
                                item.supplierPublicId(),
                                item.itemPublicId(),
                                item.bucket()
                        )
                        .orElseGet(() -> new LotExpiryAggregation(
                                item.aggregationDate(),
                                item.supplierPublicId(),
                                item.itemPublicId(),
                                item.bucket()
                        ));

                aggregation.add(1L, item.qty() == null ? BigDecimal.ZERO : item.qty());
                lotExpiryAggregationRepository.save(aggregation);
            }
        };
    }

    private LocalDate resolveExpiryDate(Lot lot) {

        if (lot.getExpiredAt() != null) {
            return lot.getExpiredAt().toLocalDate();
        }

        if (lot.getManufacturedAt() == null) { // 제조일 없으면 null
            return null;
        }

        Optional<SupplyItem> itemOptional = supplyItemRepository.findByPublicIdAndStatusIn(
                lot.getItemPublicId(),
                java.util.List.of(Status.ACTIVE, Status.DEACTIVE)
        );

        if (itemOptional.isEmpty()) {
            return null;
        }

        SupplyItem item = itemOptional.get();
        if (item.getShelfLifeDays() == null) {
            return null;
        }

        LocalDateTime manufacturedAt = lot.getManufacturedAt();
        return manufacturedAt.toLocalDate().plusDays(item.getShelfLifeDays());
    }
}
