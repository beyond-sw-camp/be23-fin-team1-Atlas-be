package com.ozz.atlas.supply.supplier.certificate.controller;

import com.ozz.atlas.supply.supplier.certificate.dtos.CreateSupplierCertificateRequestDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.SupplierCertificateResponseDto;
import com.ozz.atlas.supply.supplier.certificate.dtos.UpdateSupplierCertificateRequestDto;
import com.ozz.atlas.supply.supplier.certificate.service.SupplierCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supply")
public class SupplierCertificateController {

    private final SupplierCertificateService supplierCertificateService;

    @PostMapping("/suppliers/{supplierId}/certificates")
    public ResponseEntity<SupplierCertificateResponseDto> createSupplierCertificate(
            @PathVariable Long supplierId,
            @Valid @RequestBody CreateSupplierCertificateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierCertificateService.createSupplierCertificate(supplierId, request));
    }

    @GetMapping("/suppliers/{supplierId}/certificates")
    public ResponseEntity<List<SupplierCertificateResponseDto>> getCertificatesBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificatesBySupplier(supplierId));
    }

    @GetMapping("/certificates/{publicId}")
    public ResponseEntity<SupplierCertificateResponseDto> getCertificate(@PathVariable String publicId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificate(publicId));
    }

    @PutMapping("/certificates/{publicId}")
    public ResponseEntity<SupplierCertificateResponseDto> updateSupplierCertificate(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateSupplierCertificateRequestDto request) {
        return ResponseEntity.ok(supplierCertificateService.updateSupplierCertificate(publicId, request));
    }

    @DeleteMapping("/certificates/{publicId}")
    public ResponseEntity<Void> deleteSupplierCertificate(@PathVariable String publicId) {
        supplierCertificateService.deleteSupplierCertificate(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/certificates/{publicId}/verify")
    public ResponseEntity<Void> verifyCertificate(@PathVariable String publicId) {
        supplierCertificateService.verifyCertificate(publicId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/certificates/expiring")
    public ResponseEntity<List<SupplierCertificateResponseDto>> getExpiringCertificates(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(supplierCertificateService.getExpiringCertificates(days));
    }
}