package com.ozz.atlas.supply.onboarding.controller;

import com.ozz.atlas.supply.onboarding.dtos.CreateOnboardingRequest;
import com.ozz.atlas.supply.onboarding.dtos.RejectOnboardingRequest;
import com.ozz.atlas.supply.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/supplier-onboarding-requests")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    public ResponseEntity<?> createSupplierRequest(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-User-Public-Id") String requestedByUserPublicId,
            @Valid @RequestBody CreateOnboardingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.createSupplier(organizationPublicId, requestedByUserPublicId, request));
    }

    @GetMapping("/{requestPublicId}")
    public ResponseEntity<?> getRequest(@PathVariable String requestPublicId) {
        return ResponseEntity.ok(onboardingService.getRequest(requestPublicId));
    }

    @GetMapping
    public ResponseEntity<?> getRequestList(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(onboardingService.getRequestList(pageable));
    }

    @PatchMapping("/{requestPublicId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable String requestPublicId,
            @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId
    ) {
        return ResponseEntity.ok(onboardingService.approveRequest(requestPublicId, reviewedByUserPublicId));
    }

    @PatchMapping("/{requestPublicId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String requestPublicId,
            @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId,
            @Valid @RequestBody RejectOnboardingRequest request
    ) {
        return ResponseEntity.ok(onboardingService.rejectRequest(requestPublicId, reviewedByUserPublicId, request));
    }
}
