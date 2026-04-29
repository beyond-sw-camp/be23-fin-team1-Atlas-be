package com.ozz.atlas.supply.batch.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.batch.domain.ExpiryWarningType;
import com.ozz.atlas.supply.batch.domain.SupplyExpiryWarning;
import com.ozz.atlas.supply.batch.repository.SupplyExpiryWarningRepository;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpiryWarningEventService {

    private final SupplyExpiryWarningRepository supplyExpiryWarningRepository;
    private final SupplierCertificateRepository supplierCertificateRepository;
    private final SupplierRepository supplierRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

    @Transactional
    public void publishExpiredCertificateEvents(LocalDate runDate) {
        LocalDate targetDate = runDate != null ? runDate : LocalDate.now();

        for (SupplierCertificate certificate : supplierCertificateRepository
                .findByExpiredAtLessThanEqualAndCertificateStatus(targetDate, CertificateStatus.APPROVED)) {
            SupplySupplier supplier = resolveSupplier(certificate.getSupplierPublicId());
            certificate.expire();

            outboxEventAppender.append(
                    supplyDomainEventFactory.create(
                            KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE,
                            EventTypes.SUPPLIER_CERTIFICATE_EXPIRED,
                            AggregateType.SUPPLIER_CERTIFICATE,
                            certificate.getPublicId(),
                            null,
                            supplier != null ? supplier.getOrganizationPublicId() : null,
                            supplyChainContextResolver.fromSupplier(supplier),
                            supplyDomainEventFactory.payload(
                                    certificate.getPublicId(),
                                    certificate.getCertificateNo(),
                                    certificate.getCertificateStatus().name(),
                                    "협력사 인증서 만료",
                                    "협력사 인증서 만료 시",
                                    null
                            )
                    )
            );
        }
    }

    private SupplySupplier resolveSupplier(String supplierPublicId) {
        if (supplierPublicId == null || supplierPublicId.isBlank()) {
            return null;
        }
        return supplierRepository.findByPublicId(supplierPublicId).orElse(null);
    }

    private String supplierOrganizationPublicId(String supplierPublicId) {
        SupplySupplier supplier = resolveSupplier(supplierPublicId);
        return supplier != null ? supplier.getOrganizationPublicId() : null;
    }
}
