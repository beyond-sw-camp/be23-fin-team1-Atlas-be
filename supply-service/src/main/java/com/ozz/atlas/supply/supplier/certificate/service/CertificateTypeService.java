package com.ozz.atlas.supply.supplier.certificate.service;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import com.ozz.atlas.supply.supplier.certificate.dtos.CertificateTypeResponseDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.CreateCertificateTypeRequestDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.UpdateCertificateTypeRequestDto;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateErrorCode;
import com.ozz.atlas.supply.supplier.certificate.exception.CertificateException;
import com.ozz.atlas.supply.supplier.certificate.repository.CertificateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateTypeService {

    private final CertificateTypeRepository certificateTypeRepository;

    @Transactional
    public CertificateTypeResponseDto createCertificateType(CreateCertificateTypeRequestDto request) {
        if (certificateTypeRepository.findByCertificateCode(request.getCertificateCode()).isPresent()) {
            throw new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_DUPLICATED);
        }

        CertificateType type = CertificateType.builder()
                .certificateCode(request.getCertificateCode())
                .certificateName(request.getCertificateName())
                .scopeType(request.getScopeType())
                .requiredYn(request.isRequiredYn())
                .activeYn(request.isActiveYn())
                .build();

        return CertificateTypeResponseDto.from(certificateTypeRepository.save(type));
    }

    public List<CertificateTypeResponseDto> getAllCertificateTypes() {
        return certificateTypeRepository.findAll().stream()
                .map(CertificateTypeResponseDto::from)
                .collect(Collectors.toList());
    }

    public CertificateTypeResponseDto getCertificateType(Long id) {
        CertificateType type = certificateTypeRepository.findById(id)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));
        return CertificateTypeResponseDto.from(type);
    }

    @Transactional
    public CertificateTypeResponseDto updateCertificateType(Long id, UpdateCertificateTypeRequestDto request) {
        CertificateType type = certificateTypeRepository.findById(id)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));

        type.update(request.getCertificateName(), request.getScopeType(), request.getRequiredYn(), request.getActiveYn());
        return CertificateTypeResponseDto.from(type);
    }

    @Transactional
    public void deleteCertificateType(Long id) {
        CertificateType type = certificateTypeRepository.findById(id)
                .orElseThrow(() -> new CertificateException(CertificateErrorCode.CERTIFICATE_TYPE_NOT_FOUND));
        type.update(null, null, null, false); // Soft delete or deactivate
    }
}