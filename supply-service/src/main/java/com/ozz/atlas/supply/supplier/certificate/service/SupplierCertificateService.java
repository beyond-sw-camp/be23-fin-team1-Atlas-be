package com.ozz.atlas.supply.supplier.certificate.service;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import com.ozz.atlas.supply.supplier.certificate.dtos.CreateSupplierCertificateRequestDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.SupplierCertificateResponseDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.UpdateSupplierCertificateRequestDto;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateErrorCode;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateException;
import com.ozz.atlas.supply.supplier.certificate.repository.CertificateTypeRepository;
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

    @Transactional
    public SupplierCertificateResponseDto createSupplierCertificate(Long supplierId, CreateSupplierCertificateRequestDto request) {
        if (request.getIssuedAt() != null && request.getExpiredAt() != null && request.getExpiredAt().isBefore(request.getIssuedAt())) {
            throw new CertificateException(CertificateErrorCode.INVALID_CERTIFICATE_DATES);
        }

        CertificateType type = certificateTypeRepository.findById(request.getCertificateTypeId())
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));

        SupplierCertificate cert = SupplierCertificate.builder()
                .supplierId(supplierId)
                .certificateType(type)
                .certificateNo(request.getCertificateNo())
                .issuedAt(request.getIssuedAt())
                .expiredAt(request.getExpiredAt())
                .issuerName(request.getIssuerName())
                .build();

        return SupplierCertificateResponseDto.from(supplierCertificateRepository.save(cert));
    }

    public List<SupplierCertificateResponseDto> getCertificatesBySupplier(Long supplierId) {
        return supplierCertificateRepository.findBySupplierId(supplierId).stream()
                .map(SupplierCertificateResponseDto::from)
                .collect(Collectors.toList());
    }

    public SupplierCertificateResponseDto getCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        return SupplierCertificateResponseDto.from(cert);
    }

    @Transactional
    public SupplierCertificateResponseDto updateSupplierCertificate(String publicId, UpdateSupplierCertificateRequestDto request) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));

        if (request.getIssuedAt() != null && request.getExpiredAt() != null && request.getExpiredAt().isBefore(request.getIssuedAt())) {
            throw new CertificateException(CertificateErrorCode.INVALID_CERTIFICATE_DATES);
        }

        cert.update(request.getCertificateNo(), request.getIssuedAt(), request.getExpiredAt(), request.getIssuerName());
        return SupplierCertificateResponseDto.from(cert);
    }

    @Transactional
    public void deleteSupplierCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        supplierCertificateRepository.delete(cert);
    }

    @Transactional
    public void verifyCertificate(String publicId) {
        SupplierCertificate cert = supplierCertificateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.SUPPLIER_CERTIFICATE_NOT_FOUND));
        cert.verify();
    }

    public List<SupplierCertificateResponseDto> getExpiringCertificates(int days) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);
        return supplierCertificateRepository.findByExpiredAtBetween(today, targetDate).stream()
                .map(SupplierCertificateResponseDto::from)
                .collect(Collectors.toList());
    }
}