package com.ozz.atlas.supply.supplier.certificate.service;

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
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public SupplierCertificateResponseDto createSupplierCertificate(String supplierPublicId, CreateSupplierCertificateRequestDto request, String actorPublicId) {
        if (request.getIssuedAt() != null && request.getExpiredAt() != null && request.getExpiredAt().isBefore(request.getIssuedAt())) {
            throw new CertificateException(CertificateErrorCode.INVALID_CERTIFICATE_DATES);
        }

        CertificateType type = certificateTypeRepository.findByPublicId(request.getCertificateTypePublicId())
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));

        SupplierCertificate cert = SupplierCertificate.builder()
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

        return SupplierCertificateResponseDto.from(savedCert);
    }

    public List<SupplierCertificateResponseDto> getCertificatesBySupplier(String supplierPublicId) {
        return supplierCertificateRepository.findBySupplierPublicId(supplierPublicId).stream()
                .map(SupplierCertificateResponseDto::from)
                .collect(Collectors.toList());
    }

    public SupplierCertificateResponseDto getCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        return SupplierCertificateResponseDto.from(cert);
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

        return SupplierCertificateResponseDto.from(cert);
    }

    @Transactional
    public void deleteSupplierCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        supplierCertificateRepository.delete(cert);
    }

    @Transactional
    public void approveCertificate(String publicId, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        
        CertificateStatus beforeStatus = cert.getCertificateStatus();
        cert.approve();
        
        saveHistory(cert.getId(), "APPROVE", beforeStatus, cert.getCertificateStatus(), "관리자 승인 처리", actorPublicId);
    }

    @Transactional
    public void rejectCertificate(String publicId, RejectCertificateRequestDto request, String actorPublicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        
        CertificateStatus beforeStatus = cert.getCertificateStatus();
        cert.reject(request.getRejectReason());
        
        saveHistory(cert.getId(), "REJECT", beforeStatus, cert.getCertificateStatus(), request.getRejectReason(), actorPublicId);
    }

    public List<SupplierCertificateResponseDto> getExpiringCertificates(int days) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);
        return supplierCertificateRepository.findByExpiredAtBetween(today, targetDate).stream()
                .map(SupplierCertificateResponseDto::from)
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
}