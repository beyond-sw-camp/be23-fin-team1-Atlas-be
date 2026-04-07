package com.ozz.atlas.supply.supplier.certificate.controller;

import com.ozz.atlas.supply.supplier.certificate.dtos.*;
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
            @Valid @RequestBody CreateSupplierCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierCertificateService.createSupplierCertificate(supplierId, request, actorPublicId));
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
            @Valid @RequestBody UpdateSupplierCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.ok(supplierCertificateService.updateSupplierCertificate(publicId, request, actorPublicId));
    }

    @DeleteMapping("/certificates/{publicId}")
    public ResponseEntity<Void> deleteSupplierCertificate(@PathVariable String publicId) {
        supplierCertificateService.deleteSupplierCertificate(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/certificates/{publicId}/approve")
    public ResponseEntity<Void> approveCertificate(
            @PathVariable String publicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        supplierCertificateService.approveCertificate(publicId, actorPublicId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/certificates/{publicId}/reject")
    public ResponseEntity<Void> rejectCertificate(
            @PathVariable String publicId,
            @Valid @RequestBody RejectCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        supplierCertificateService.rejectCertificate(publicId, request, actorPublicId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/certificates/expiring")
    public ResponseEntity<List<SupplierCertificateResponseDto>> getExpiringCertificates(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(supplierCertificateService.getExpiringCertificates(days));
    }

    @GetMapping("/certificates/{publicId}/histories")
    public ResponseEntity<List<SupplierCertificateHistoryResponseDto>> getCertificateHistories(@PathVariable String publicId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificateHistories(publicId));
    }
}