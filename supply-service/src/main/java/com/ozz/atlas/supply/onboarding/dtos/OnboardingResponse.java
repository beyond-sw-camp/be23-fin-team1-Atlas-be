package com.ozz.atlas.supply.onboarding.dtos;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequest;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestStatus;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestType;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierTierLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "협력사 온보딩 요청 응답")
public class OnboardingResponse {

    @Schema(description = "온보딩 요청 공개 식별자", example = "onb_01HZY0AA11BB22CC33DD44EE55")
    private String requestPublicId;
    @Schema(description = "요청 유형", example = "CREATE")
    private OnboardingRequestType requestType;
    @Schema(description = "요청 상태", example = "PENDING")
    private OnboardingRequestStatus requestStatus;
    @Schema(description = "요청자 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String requestedByUserPublicId;
    @Schema(description = "검토자 사용자 공개 식별자", example = "usr_01HZXREVIEWER1234567890", nullable = true)
    private String reviewedByUserPublicId;
    @Schema(description = "요청 시각", example = "2026-04-17T09:30:00")
    private LocalDateTime requestedAt;
    @Schema(description = "검토 시각", example = "2026-04-17T11:00:00", nullable = true)
    private LocalDateTime reviewedAt;
    @Schema(description = "반려 사유", example = "필수 인증 서류 누락", nullable = true)
    private String rejectReason;

    @Schema(description = "생성 또는 연결된 협력사 공개 식별자", example = "sup_01HZY0SUPPLIER123456789")
    private String supplierPublicId;
    @Schema(description = "요청을 생성한 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;
    @Schema(description = "협력사 코드", example = "SUP-FOOD-001")
    private String supplierCode;
    @Schema(description = "협력사명", example = "Fresh Chain Co.")
    private String supplierName;
    @Schema(description = "협력사 tier 수준", example = "TIER1")
    private SupplierTierLevel tierLevel;
    @Schema(description = "협력사 운영 상태", example = "ACTIVE")
    private SupplierStatus supplierStatus;
    @Schema(description = "협력사 승인 상태", example = "PENDING")
    private ApprovalStatus approvalStatus;
    @Schema(description = "주요 담당자 이름", example = "Park Jisoo")
    private String primaryContactName;
    @Schema(description = "주요 담당자 이메일", example = "partner@freshchain.com")
    private String primaryContactEmail;
    @Schema(description = "주요 담당자 연락처", example = "02-3456-7890")
    private String primaryContactPhone;

    public static OnboardingResponse fromEntity(OnboardingRequest request) {
        return OnboardingResponse.builder()
                .requestPublicId(request.getPublicId())
                .requestType(request.getRequestType())
                .requestStatus(request.getRequestStatus())
                .requestedByUserPublicId(request.getRequestedByUserPublicId())
                .reviewedByUserPublicId(request.getReviewedByUserPublicId())
                .requestedAt(request.getRequestedAt())
                .reviewedAt(request.getReviewedAt())
                .rejectReason(request.getRejectReason())
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
