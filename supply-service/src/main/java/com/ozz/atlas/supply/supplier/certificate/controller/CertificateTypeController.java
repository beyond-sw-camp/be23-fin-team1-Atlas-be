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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/certificate-types")
@Tag(name = "CertificateType")
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;

    @Operation(summary = "인증서 유형 생성")
    @PostMapping
    public ResponseEntity<CertificateTypeResponseDto> createCertificateType(@Valid @RequestBody CreateCertificateTypeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificateTypeService.createCertificateType(request));
    }

    @Operation(summary = "인증서 유형 목록 조회")
    @GetMapping
    public ResponseEntity<List<CertificateTypeResponseDto>> getAllCertificateTypes() {
        return ResponseEntity.ok(certificateTypeService.getAllCertificateTypes());
    }

    @Operation(summary = "인증서 유형 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<CertificateTypeResponseDto> getCertificateType(@PathVariable String id) {
        return ResponseEntity.ok(certificateTypeService.getCertificateType(id));
    }

    @Operation(summary = "인증서 유형 수정")
    @PutMapping("/{id}")
    public ResponseEntity<CertificateTypeResponseDto> updateCertificateType(
            @PathVariable String id,
            @Valid @RequestBody UpdateCertificateTypeRequestDto request) {
        return ResponseEntity.ok(certificateTypeService.updateCertificateType(id, request));
    }

    @Operation(summary = "인증서 유형 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificateType(@PathVariable String id) {
        certificateTypeService.deleteCertificateType(id);
        return ResponseEntity.noContent().build();
    }
}