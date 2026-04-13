package com.ozz.atlas.supply.onboarding.service;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequest;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestStatus;
import com.ozz.atlas.supply.onboarding.dtos.CreateOnboardingRequest;
import com.ozz.atlas.supply.onboarding.dtos.OnboardingResponse;
import com.ozz.atlas.supply.onboarding.dtos.RejectOnboardingRequest;
import com.ozz.atlas.supply.onboarding.exception.OnboardingErrorCode;
import com.ozz.atlas.supply.onboarding.exception.OnboardingException;
import com.ozz.atlas.supply.onboarding.repository.OnboardingRequestRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private static final List<OnboardingRequestStatus> READABLE_REQUEST_STATUSES =
            List.of(OnboardingRequestStatus.REQUESTED, OnboardingRequestStatus.APPROVED, OnboardingRequestStatus.REJECTED);

    private static final String ADMIN_ROLE = "ADMIN";

    private final OnboardingRequestRepository onboardingRequestRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierSearchService supplierSearchService;

    public OnboardingResponse createSupplier(
            String organizationPublicId,
            String requestedByUserPublicId,
            CreateOnboardingRequest request
    ) {
        validateCreateActor(organizationPublicId, requestedByUserPublicId);

        if (supplierRepository.existsBySupplierCodeAndSupplierStatusNot(
                request.getSupplierCode(),
                SupplierStatus.TERMINATED
        )) {
            throw new OnboardingException(OnboardingErrorCode.SUPPLIER_CODE_ALREADY_EXISTS);
        }

        SupplySupplier supplier = SupplySupplier.create(
                organizationPublicId,
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getTierLevel(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );

        SupplySupplier savedSupplier = supplierRepository.save(supplier);

        OnboardingRequest onboardingRequest = OnboardingRequest.create(
                savedSupplier,
                requestedByUserPublicId
        );

        return OnboardingResponse.fromEntity(onboardingRequestRepository.save(onboardingRequest));
    }

    @Transactional(readOnly = true)
    public OnboardingResponse getRequest(
            String requestPublicId,
            String organizationPublicId,
            String userRole
    ) {
        validateReadActor(organizationPublicId, userRole);

        OnboardingRequest request = getReadableRequest(requestPublicId);

        if (!isAdmin(userRole) && !request.getSupplier().getOrganizationPublicId().equals(organizationPublicId)) {
            throw new OnboardingException(OnboardingErrorCode.ACCESS_DENIED);
        }

        return OnboardingResponse.fromEntity(request);
    }

    @Transactional(readOnly = true)
    public Page<OnboardingResponse> getRequestList(
            Pageable pageable,
            String organizationPublicId,
            String userRole
    ) {
        validateReadActor(organizationPublicId, userRole);

        if (isAdmin(userRole)) {
            return onboardingRequestRepository
                    .findAllByRequestStatusInAndSupplier_SupplierStatusNot(
                            READABLE_REQUEST_STATUSES,
                            SupplierStatus.TERMINATED,
                            pageable
                    )
                    .map(OnboardingResponse::fromEntity);
        }

        return onboardingRequestRepository
                .findAllByRequestStatusInAndSupplier_OrganizationPublicIdAndSupplier_SupplierStatusNot(
                        READABLE_REQUEST_STATUSES,
                        organizationPublicId,
                        SupplierStatus.TERMINATED,
                        pageable
                )
                .map(OnboardingResponse::fromEntity);
    }

    public OnboardingResponse approveRequest(
            String requestPublicId,
            String reviewedByUserPublicId,
            String userRole
    ) {
        validateAdminReviewer(reviewedByUserPublicId, userRole);

        OnboardingRequest request = getPendingRequest(requestPublicId);

        request.approve(reviewedByUserPublicId);
        request.getSupplier().approve();
        supplierSearchService.saveSupplierDocument(request.getSupplier());

        return OnboardingResponse.fromEntity(request);
    }

    public OnboardingResponse rejectRequest(
            String requestPublicId,
            String reviewedByUserPublicId,
            String userRole,
            RejectOnboardingRequest rejectRequest
    ) {
        validateAdminReviewer(reviewedByUserPublicId, userRole);

        OnboardingRequest request = getPendingRequest(requestPublicId);

        request.reject(reviewedByUserPublicId, rejectRequest.getRejectReason());
        request.getSupplier().reject();

        return OnboardingResponse.fromEntity(request);
    }

    private OnboardingRequest getReadableRequest(String requestPublicId) {
        return onboardingRequestRepository
                .findByPublicIdAndRequestStatusInAndSupplier_SupplierStatusNot(
                        requestPublicId,
                        READABLE_REQUEST_STATUSES,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new OnboardingException(OnboardingErrorCode.REQUEST_NOT_FOUND));
    }

    private OnboardingRequest getPendingRequest(String requestPublicId) {
        return onboardingRequestRepository
                .findByPublicIdAndRequestStatusAndSupplier_SupplierStatusNot(
                        requestPublicId,
                        OnboardingRequestStatus.REQUESTED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new OnboardingException(OnboardingErrorCode.REVIEWABLE_REQUEST_NOT_FOUND));
    }

    private void validateCreateActor(String organizationPublicId, String requestedByUserPublicId) {
        if (!hasText(organizationPublicId) || !hasText(requestedByUserPublicId)) {
            throw new OnboardingException(OnboardingErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private void validateReadActor(String organizationPublicId, String userRole) {
        if (!hasText(userRole)) {
            throw new OnboardingException(OnboardingErrorCode.INVALID_ACTOR_HEADER);
        }

        if (!isAdmin(userRole) && !hasText(organizationPublicId)) {
            throw new OnboardingException(OnboardingErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private void validateAdminReviewer(String reviewedByUserPublicId, String userRole) {
        if (!hasText(reviewedByUserPublicId) || !hasText(userRole)) {
            throw new OnboardingException(OnboardingErrorCode.INVALID_ACTOR_HEADER);
        }

        if (!isAdmin(userRole)) {
            throw new OnboardingException(OnboardingErrorCode.ADMIN_ONLY_ACTION);
        }
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
