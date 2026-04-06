package com.ozz.atlas.supply.onboarding.dtos;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequest;
import java.time.LocalDateTime;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestStatus;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestType;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponse {

    private Long requestId;
    private OnboardingRequestType requestType;
    private OnboardingRequestStatus requestStatus;
    private String requestedByUserPublicId;
    private String reviewedByUserPublicId;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private String rejectReason;

    private Long supplierId;
    private String supplierPublicId;
    private String organizationPublicId;
    private String supplierCode;
    private String supplierName;
    private Integer tierLevel;
    private SupplierStatus supplierStatus;
    private ApprovalStatus approvalStatus;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;

    public static OnboardingResponse fromEntity(OnboardingRequest request) {
        return OnboardingResponse.builder()
                .requestId(request.getId())
                .requestType(request.getRequestType())
                .requestStatus(request.getRequestStatus())
                .requestedByUserPublicId(request.getRequestedByUserPublicId())
                .reviewedByUserPublicId(request.getReviewedByUserPublicId())
                .requestedAt(request.getRequestedAt())
                .reviewedAt(request.getReviewedAt())
                .rejectReason(request.getRejectReason())
                .supplierId(request.getSupplier().getId())
                .supplierPublicId(request.getSupplier().getPublicId())
                .organizationPublicId(request.getSupplier().getOrganizationPublicId())
                .supplierCode(request.getSupplier().getSupplierCode())
                .supplierName(request.getSupplier().getSupplierName())
                .tierLevel(request.getSupplier().getTierLevel())
                .supplierStatus(request.getSupplier().getSupplierStatus())
                .approvalStatus(request.getSupplier().getApprovalStatus())
                .primaryContactName(request.getSupplier().getPrimaryContactName())
                .primaryContactEmail(request.getSupplier().getPrimaryContactEmail())
                .primaryContactPhone(request.getSupplier().getPrimaryContactPhone())
                .build();
    }
}