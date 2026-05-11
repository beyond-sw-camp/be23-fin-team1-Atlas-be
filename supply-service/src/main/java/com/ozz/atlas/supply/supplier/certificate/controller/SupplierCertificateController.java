package com.ozz.atlas.supply.supplier.certificate.controller;

import com.ozz.atlas.supply.supplier.certificate.dtos.*;
import com.ozz.atlas.supply.supplier.certificate.service.SupplierCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply")
@Tag(name = "SupplierCertificate")
public class SupplierCertificateController {

    private final SupplierCertificateService supplierCertificateService;

    @Operation(summary = "협력사 인증서 생성")
    @PostMapping("/suppliers/{supplierPublicId}/certificates")
    public ResponseEntity<SupplierCertificateResponseDto> createSupplierCertificate(
            @PathVariable String supplierPublicId,
            @Valid @RequestBody CreateSupplierCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierCertificateService.createSupplierCertificate(supplierPublicId, request, actorPublicId));
    }

    @Operation(summary = "협력사 인증서 전체 목록 조회")
    @GetMapping("/certificates")
    public ResponseEntity<Page<SupplierCertificateResponseDto>> getAllCertificates(
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(supplierCertificateService.searchCertificatesForReview(pageable, reviewStatus, keyword));
    }

    @Operation(summary = "협력사별 인증서 목록 조회")
    @GetMapping("/suppliers/{supplierPublicId}/certificates")
    public ResponseEntity<List<SupplierCertificateResponseDto>> getCertificatesBySupplier(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificatesBySupplier(supplierPublicId));
    }

    @Operation(summary = "협력사별 인증서 요약 조회")
    @GetMapping("/suppliers/{supplierPublicId}/certificates/summary")
    public ResponseEntity<SupplierCertificateSummaryResponseDto> getCertificateSummaryBySupplier(
            @PathVariable String supplierPublicId,
            @RequestParam(defaultValue = "30") int expiringWithinDays) {
        return ResponseEntity.ok(
                supplierCertificateService.getCertificateSummaryBySupplier(supplierPublicId, expiringWithinDays)
        );
    }

    @Operation(summary = "협력사 인증서 상세 조회")
    @GetMapping("/certificates/{publicId}")
    public ResponseEntity<SupplierCertificateResponseDto> getCertificate(@PathVariable String publicId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificate(publicId));
    }

    @Operation(summary = "협력사 인증서 수정")
    @PutMapping("/certificates/{publicId}")
    public ResponseEntity<SupplierCertificateResponseDto> updateSupplierCertificate(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateSupplierCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.ok(supplierCertificateService.updateSupplierCertificate(publicId, request, actorPublicId));
    }

    @Operation(summary = "협력사 인증서 삭제")
    @DeleteMapping("/certificates/{publicId}")
    public ResponseEntity<Void> deleteSupplierCertificate(
            @PathVariable String publicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId
    ) {
        supplierCertificateService.deleteSupplierCertificate(publicId, actorPublicId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "협력사 인증서 승인")
    @PatchMapping("/certificates/{publicId}/approve")
    public ResponseEntity<Void> approveCertificate(
            @PathVariable String publicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String reviewerOrganizationPublicId) {
        supplierCertificateService.approveCertificate(publicId, actorPublicId, reviewerOrganizationPublicId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "협력사 인증서 거절")
    @PatchMapping("/certificates/{publicId}/reject")
    public ResponseEntity<Void> rejectCertificate(
            @PathVariable String publicId,
            @Valid @RequestBody RejectCertificateRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String reviewerOrganizationPublicId) {
        supplierCertificateService.rejectCertificate(publicId, request, actorPublicId, reviewerOrganizationPublicId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "만료 임박 인증서 목록 조회")
    @GetMapping("/certificates/expiring")
    public ResponseEntity<List<SupplierCertificateResponseDto>> getExpiringCertificates(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(supplierCertificateService.getExpiringCertificates(days));
    }

    @Operation(summary = "협력사 인증서 이력 조회")
    @GetMapping("/certificates/{publicId}/histories")
    public ResponseEntity<List<SupplierCertificateHistoryResponseDto>> getCertificateHistories(@PathVariable String publicId) {
        return ResponseEntity.ok(supplierCertificateService.getCertificateHistories(publicId));
    }
}
