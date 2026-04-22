// 협력사 등록 프로세스 변경으로 auth-service에서 협력사 등록 예정
package com.ozz.atlas.supply.onboarding.controller;

import com.ozz.atlas.supply.onboarding.dtos.CreateOnboardingRequest;
import com.ozz.atlas.supply.onboarding.dtos.OnboardingResponse;
import com.ozz.atlas.supply.onboarding.dtos.RejectOnboardingRequest;
import com.ozz.atlas.supply.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    @Operation(
            summary = "협력사 온보딩 요청 생성",
            description = "신규 협력사 등록 또는 심사를 위한 온보딩 요청을 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateOnboardingRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "supplierCode": "SUP-FOOD-001",
                                              "supplierName": "Fresh Chain Co.",
                                              "primaryContactName": "Park Jisoo",
                                              "primaryContactEmail": "partner@freshchain.com",
                                              "primaryContactPhone": "02-3456-7890"
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "요청 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = OnboardingResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "requestPublicId": "onb_01HZY0AA11BB22CC33DD44EE55",
                                              "requestType": "CREATE",
                                              "requestStatus": "PENDING",
                                              "requestedByUserPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "reviewedByUserPublicId": null,
                                              "requestedAt": "2026-04-17T09:30:00",
                                              "reviewedAt": null,
                                              "rejectReason": null,
                                              "supplierPublicId": "sup_01HZY0SUPPLIER123456789",
                                              "organizationPublicId": "org_01HZX9X5D4P2Q7F8R9S1T2U3V4",
                                              "supplierCode": "SUP-FOOD-001",
                                              "supplierName": "Fresh Chain Co.",
                                              "supplierStatus": "ACTIVE",
                                              "approvalStatus": "PENDING",
                                              "primaryContactName": "Park Jisoo",
                                              "primaryContactEmail": "partner@freshchain.com",
                                              "primaryContactPhone": "02-3456-7890"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<?> createSupplierRequest(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader("X-User-Public-Id") String requestedByUserPublicId,
            @Valid @RequestBody CreateOnboardingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.createSupplier(
                        organizationPublicId,
                        organizationType,
                        requestedByUserPublicId,
                        request
                ));
    }

    @GetMapping("/{requestPublicId}")
    public ResponseEntity<?> getRequest(
            @PathVariable String requestPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(
                onboardingService.getRequest(
                        requestPublicId,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getRequestList(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader("X-User-Role") String userRole,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                onboardingService.getRequestList(
                        pageable,
                        organizationPublicId,
                        organizationType,
                        userRole
                )
        );
    }

    @PatchMapping("/{requestPublicId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable String requestPublicId,
            @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(
                onboardingService.approveRequest(requestPublicId, reviewedByUserPublicId, userRole)
        );
    }

    @PatchMapping("/{requestPublicId}/reject")
    @Operation(
            summary = "온보딩 요청 반려",
            description = "반려 사유를 기록하고 온보딩 요청을 거절한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RejectOnboardingRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "rejectReason": "필수 인증 서류가 누락되었습니다."
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<?> rejectRequest(
            @PathVariable String requestPublicId,
            @RequestHeader("X-User-Public-Id") String reviewedByUserPublicId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody RejectOnboardingRequest request
    ) {
        return ResponseEntity.ok(
                onboardingService.rejectRequest(requestPublicId, reviewedByUserPublicId, userRole, request)
        );
    }
}
