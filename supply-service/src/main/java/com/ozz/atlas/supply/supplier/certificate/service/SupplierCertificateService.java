package com.ozz.atlas.supply.supplier.certificate.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateHistory;
import com.ozz.atlas.supply.supplier.certificate.dtos.*;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateErrorCode;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateException;
import com.ozz.atlas.supply.supplier.certificate.repository.CertificateTypeRepository;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateHistoryRepository;
import com.ozz.atlas.supply.supplier.certificate.repository.SupplierCertificateRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierCertificateService {

    private final SupplierCertificateRepository supplierCertificateRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final SupplierCertificateHistoryRepository supplierCertificateHistoryRepository;
    private final SupplierRepository supplierRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

    @Transactional
    public SupplierCertificateResponseDto createSupplierCertificate(String supplierPublicId, CreateSupplierCertificateRequestDto request, String actorPublicId) {
        if (request.getIssuedAt() != null && request.getExpiredAt() != null && request.getExpiredAt().isBefore(request.getIssuedAt())) {
            throw new CertificateException(CertificateErrorCode.INVALID_CERTIFICATE_DATES);
        }

        CertificateType type = certificateTypeRepository.findByPublicId(request.getCertificateTypePublicId())
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));

        SupplySupplier supplier = supplierRepository.findByPublicId(supplierPublicId)
                .orElseThrow(() -> new com.ozz.atlas.supply.supplier.exception.SupplierException(com.ozz.atlas.supply.supplier.exception.SupplierErrorCode.SUPPLIER_NOT_FOUND));

        SupplierCertificate cert = SupplierCertificate.builder()
                .supplierId(supplier.getId())
                .supplierPublicId(supplierPublicId)
                .certificateType(type)
                .certificateNo(request.getCertificateNo())
                .issuedAt(request.getIssuedAt())
                .expiredAt(request.getExpiredAt())
                .issuerName(request.getIssuerName())
                .attachmentPublicId(request.getAttachmentPublicId())
                .build();

        SupplierCertificate savedCert = supplierCertificateRepository.save(cert);
        
        saveHistory(savedCert.getId(), "CREATE", null, savedCert.getCertificateStatus(), "인증서 등록", actorPublicId);
        appendCertificateEvent(
                EventTypes.SUPPLIER_CERTIFICATE_CREATED,
                savedCert,
                supplier,
                actorPublicId,
                "협력사 인증서 생성",
                "협력사 인증서 생성 시"
        );

        return toResponseDto(savedCert);
    }

    public Page<SupplierCertificateResponseDto> getAllCertificates(Pageable pageable) {
        return supplierCertificateRepository.findAll(pageable).map(this::toResponseDto);
    }

    public List<SupplierCertificateResponseDto> getCertificatesBySupplier(String supplierPublicId) {
        return supplierCertificateRepository.findBySupplierPublicId(supplierPublicId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public SupplierCertificateSummaryResponseDto getCertificateSummaryBySupplier(
            String supplierPublicId,
            int expiringWithinDays
    ) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(Math.max(expiringWithinDays, 0));
        List<SupplierCertificate> certificates = supplierCertificateRepository.findBySupplierPublicId(supplierPublicId);

        long validCount = certificates.stream()
                .filter(certificate -> certificate.getCertificateStatus() == CertificateStatus.APPROVED)
                .count();
        long expiringSoonCount = certificates.stream()
                .filter(certificate -> isExpiringWithin(certificate, today, targetDate))
                .count();
        long renewalNeededCount = certificates.stream()
                .filter(certificate -> certificate.getCertificateStatus() == CertificateStatus.EXPIRED
                        || certificate.getCertificateStatus() == CertificateStatus.REVOKED)
                .count();

        return SupplierCertificateSummaryResponseDto.builder()
                .validCount(validCount)
                .expiringSoonCount(expiringSoonCount)
                .renewalNeededCount(renewalNeededCount)
                .totalCount(certificates.size())
                .build();
    }

    public SupplierCertificateResponseDto getCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        return toResponseDto(cert);
    }

    @Transactional
    public SupplierCertificateResponseDto updateSupplierCertificate(String publicId, UpdateSupplierCertificateRequestDto request, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));

        if (request.getIssuedAt() != null && request.getExpiredAt() != null && request.getExpiredAt().isBefore(request.getIssuedAt())) {
            throw new CertificateException(CertificateErrorCode.INVALID_CERTIFICATE_DATES);
        }

        CertificateStatus beforeStatus = cert.getCertificateStatus();
        cert.update(request.getCertificateNo(), request.getIssuedAt(), request.getExpiredAt(), request.getIssuerName(), request.getAttachmentPublicId());
        
        saveHistory(cert.getId(), "UPDATE", beforeStatus, cert.getCertificateStatus(), "인증서 수정 및 재심사 요청", actorPublicId);
        appendCertificateEvent(
                EventTypes.SUPPLIER_CERTIFICATE_CREATED,
                cert,
                resolveSupplier(cert),
                actorPublicId,
                "협력사 인증서 수정",
                "협력사 인증서 수정 시"
        );

        return toResponseDto(cert);
    }

    @Transactional
    public void deleteSupplierCertificate(String publicId, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        appendCertificateEvent(
                EventTypes.SUPPLIER_CERTIFICATE_REVOKED,
                cert,
                resolveSupplier(cert),
                actorPublicId,
                "협력사 인증서 철회",
                "협력사 인증서 철회 시"
        );
        supplierCertificateRepository.delete(cert);
    }

    @Transactional
    public void approveCertificate(String publicId, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        
        CertificateStatus beforeStatus = cert.getCertificateStatus();
        cert.approve();
        
        saveHistory(cert.getId(), "APPROVE", beforeStatus, cert.getCertificateStatus(), "관리자 승인 처리", actorPublicId);
        appendCertificateEvent(
                EventTypes.SUPPLIER_CERTIFICATE_APPROVED,
                cert,
                resolveSupplier(cert),
                actorPublicId,
                "협력사 인증서 승인",
                "협력사 인증서 승인 시"
        );
    }

    @Transactional
    public void rejectCertificate(String publicId, RejectCertificateRequestDto request, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        
        CertificateStatus beforeStatus = cert.getCertificateStatus();
        cert.reject(request.getRejectReason());
        
        saveHistory(cert.getId(), "REJECT", beforeStatus, cert.getCertificateStatus(), request.getRejectReason(), actorPublicId);
        appendCertificateEvent(
                EventTypes.SUPPLIER_CERTIFICATE_REJECTED,
                cert,
                resolveSupplier(cert),
                actorPublicId,
                "협력사 인증서 거절",
                "협력사 인증서 거절 시"
        );
    }

    public List<SupplierCertificateResponseDto> getExpiringCertificates(int days) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);
        return supplierCertificateRepository.findByExpiredAtBetween(today, targetDate).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SupplierCertificateHistoryResponseDto> getCertificateHistories(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
                
        return supplierCertificateHistoryRepository.findBySupplierCertificateIdOrderByRecordedAtDesc(cert.getId())
                .stream()
                .map(SupplierCertificateHistoryResponseDto::from)
                .collect(Collectors.toList());
    }

    private void saveHistory(Long certId, String actionType, CertificateStatus before, CertificateStatus after, String reason, String actor) {
        SupplierCertificateHistory history = SupplierCertificateHistory.builder()
                .supplierCertificateId(certId)
                .actionType(actionType)
                .beforeStatus(before)
                .afterStatus(after)
                .reason(reason)
                .actorPublicId(actor)
                .build();
        supplierCertificateHistoryRepository.save(history);
    }

    private boolean isExpiringWithin(SupplierCertificate certificate, LocalDate startDate, LocalDate endDate) {
        LocalDate expiredAt = certificate.getExpiredAt();
        return expiredAt != null && !expiredAt.isBefore(startDate) && !expiredAt.isAfter(endDate);
    }

    private SupplierCertificateResponseDto toResponseDto(SupplierCertificate cert) {
        String supplierName = null;
        if (cert.getSupplierPublicId() != null) {
            SupplySupplier supplier = supplierRepository.findByPublicId(cert.getSupplierPublicId()).orElse(null);
            if (supplier != null) {
                supplierName = supplier.getSupplierName();
            }
        }
        return SupplierCertificateResponseDto.from(cert, supplierName);
    }

    private void appendCertificateEvent(
            String eventType,
            SupplierCertificate certificate,
            SupplySupplier supplier,
            String actorUserPublicId,
            String eventName,
            String description
    ) {
        SupplyChainContext context = supplyChainContextResolver.fromSupplier(supplier);
        String organizationPublicId = supplier != null ? supplier.getOrganizationPublicId() : null;
        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE,
                        eventType,
                        AggregateType.SUPPLIER_CERTIFICATE,
                        certificate.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        context,
                        supplyDomainEventFactory.payload(
                                certificate.getPublicId(),
                                certificate.getCertificateNo(),
                                certificate.getCertificateStatus().name(),
                                eventName,
                                description,
                                null
                        )
                )
        );
    }

    private SupplySupplier resolveSupplier(SupplierCertificate certificate) {
        return supplierRepository.findByPublicId(certificate.getSupplierPublicId()).orElse(null);
    }
}
