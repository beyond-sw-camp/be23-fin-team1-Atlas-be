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
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

//    협력사 등록 요청
    @PostMapping("/create")
    public ResponseEntity<?> createSupplierRequest(@RequestHeader("X-Organization-Public-Id") String organizationPublicId,
                                                   @RequestHeader("X-User-Public-Id") String requestedByUserPublicId,
                                                   @Valid @RequestBody CreateOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.createSupplier(organizationPublicId, requestedByUserPublicId, request));
    }

//    협력사 등록 요청 상세 조회
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(onboardingService.getRequest(requestId));
    }

//    협력사 등록 요청 목록 조회
    @GetMapping
    public ResponseEntity<?> getRequestList(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(onboardingService.getRequestList(pageable));
    }

//    협력사 등록 요청 승인
    @PatchMapping("/approve/{requestId}")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId,
                                            @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId) {
        return ResponseEntity.ok(onboardingService.approveRequest(requestId, reviewedByUserPublicId));
    }

//    협력사 등록 요청 반려
    @PatchMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId,
                                           @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId,
                                           @Valid @RequestBody RejectOnboardingRequest request) {
        return ResponseEntity.ok(onboardingService.rejectRequest(requestId, reviewedByUserPublicId, request));
    }
}
