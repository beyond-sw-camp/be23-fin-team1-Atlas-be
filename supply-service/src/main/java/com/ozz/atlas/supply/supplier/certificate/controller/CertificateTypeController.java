package com.ozz.atlas.supply.supplier.certificate.controller;

import com.ozz.atlas.supply.supplier.certificate.dtos.CertificateTypeResponseDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.CreateCertificateTypeRequestDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.UpdateCertificateTypeRequestDto;
import com.ozz.atlas.supply.supplier.certificate.service.CertificateTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/certificate-types")
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;

    @PostMapping
    public ResponseEntity<CertificateTypeResponseDto> createCertificateType(@Valid @RequestBody CreateCertificateTypeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificateTypeService.createCertificateType(request));
    }

    @GetMapping
    public ResponseEntity<List<CertificateTypeResponseDto>> getAllCertificateTypes() {
        return ResponseEntity.ok(certificateTypeService.getAllCertificateTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateTypeResponseDto> getCertificateType(@PathVariable Long id) {
        return ResponseEntity.ok(certificateTypeService.getCertificateType(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificateTypeResponseDto> updateCertificateType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCertificateTypeRequestDto request) {
        return ResponseEntity.ok(certificateTypeService.updateCertificateType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificateType(@PathVariable Long id) {
        certificateTypeService.deleteCertificateType(id);
        return ResponseEntity.noContent().build();
    }
}