package com.ozz.atlas.supply.batch.certificate;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.batch.domain.ExpiryWarningType;
import com.ozz.atlas.supply.batch.domain.SupplyExpiryWarning;
import com.ozz.atlas.supply.batch.repository.SupplyExpiryWarningRepository;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CertificateExpiryWarningJobConfig {

    private final JobRepository jobRepository; // Job, Step 실행 이력 관리
    private final PlatformTransactionManager transactionManager; // chunk 처리할 때 트랜잭션 경계 관리
    private final EntityManagerFactory entityManagerFactory;
    private final SupplyExpiryWarningRepository supplyExpiryWarningRepository;
    private final SupplierCertificateRepository supplierCertificateRepository;
    private final SupplierRepository supplierRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

    @Bean
    public Job certificateExpiryWarningJob() {
        return new JobBuilder("certificateExpiryWarningJob", jobRepository)
                .start(certificateExpiryWarningStep())
                .build();
    }

    @Bean
    public Step certificateExpiryWarningStep() {
        return new StepBuilder("certificateExpiryWarningStep", jobRepository)
                .<SupplierCertificate, SupplyExpiryWarning>chunk(100, transactionManager)
                .reader(certificateExpiryWarningReader(null))
                .processor(certificateExpiryWarningProcessor(null))
                .writer(certificateExpiryWarningWriter())
                .build();
    }

    @Bean
    @StepScope // step 실행 시점에 만들겠다
    public JpaPagingItemReader<SupplierCertificate> certificateExpiryWarningReader(
            @Value("#{jobParameters['runDate']}") String runDate
    ) {
        // 30일 뒤 만료되는 인증서
        LocalDate targetDate = LocalDate.parse(runDate).plusDays(30);

        Map<String, Object> params = new HashMap<>();
        params.put("targetDate", targetDate);
        params.put("approvedStatus", CertificateStatus.APPROVED);

        return new JpaPagingItemReaderBuilder<SupplierCertificate>()
                .name("certificateExpiryWarningReader") // reader 이름
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100) // 한 번에 DB에서 읽을 건수
                .queryString("""
                        select c
                        from SupplierCertificate c
                        where c.expiredAt = :targetDate
                          and c.certificateStatus = :approvedStatus
                        """)
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<SupplierCertificate, SupplyExpiryWarning> certificateExpiryWarningProcessor(
            @Value("#{jobParameters['runDate']}") String runDate
    ) {
        return certificate -> {
            LocalDate warningDate = LocalDate.parse(runDate);

            boolean alreadyExists = supplyExpiryWarningRepository
                    .existsByWarningTypeAndSourcePublicIdAndWarningDate(
                            ExpiryWarningType.CERTIFICATE_EXPIRY,
                            certificate.getPublicId(),
                            warningDate
                    );

            if (alreadyExists) {
                return null;
            }

            return SupplyExpiryWarning.builder()
                    .warningType(ExpiryWarningType.CERTIFICATE_EXPIRY)
                    .sourcePublicId(certificate.getPublicId())
                    .supplierPublicId(certificate.getSupplierPublicId())
                    .title("인증서 만료 예정")
                    .message("인증서가 30일 이내 만료됩니다. certificateNo="
                            + (certificate.getCertificateNo() == null ? "-" : certificate.getCertificateNo()))
                    .expiryDate(certificate.getExpiredAt())
                    .daysRemaining((int) ChronoUnit.DAYS.between(warningDate, certificate.getExpiredAt()))
                    .warningDate(warningDate)
                    .build();
        };
    }

    @Bean
    public ItemWriter<SupplyExpiryWarning> certificateExpiryWarningWriter() {
        return chunk -> {
            supplyExpiryWarningRepository.saveAll(chunk.getItems());

            for (SupplyExpiryWarning warning : chunk.getItems()) {
                SupplierCertificate certificate = supplierCertificateRepository.findByPublicId(warning.getSourcePublicId())
                        .orElse(null);
                if (certificate == null) {
                    continue;
                }

                SupplySupplier supplier = supplierRepository.findByPublicId(certificate.getSupplierPublicId()).orElse(null);
                outboxEventAppender.append(
                        supplyDomainEventFactory.create(
                                KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE,
                                EventTypes.SUPPLIER_CERTIFICATE_EXPIRING,
                                AggregateType.SUPPLIER_CERTIFICATE,
                                certificate.getPublicId(),
                                null,
                                supplier != null ? supplier.getOrganizationPublicId() : null,
                                supplyChainContextResolver.fromSupplier(supplier),
                                supplyDomainEventFactory.payload(
                                        certificate.getPublicId(),
                                        certificate.getCertificateNo(),
                                        certificate.getCertificateStatus().name(),
                                        "협력사 인증서 만료 임박",
                                        "협력사 인증서 만료 임박 시",
                                        null
                                )
                        )
                );
            }
        };
    }
}
