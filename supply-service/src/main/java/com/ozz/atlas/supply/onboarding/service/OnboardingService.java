package com.ozz.atlas.supply.onboarding.service;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequest;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestStatus;
import com.ozz.atlas.supply.onboarding.dtos.CreateOnboardingRequest;
import com.ozz.atlas.supply.onboarding.dtos.OnboardingResponse;
import com.ozz.atlas.supply.onboarding.dtos.RejectOnboardingRequest;
import com.ozz.atlas.supply.onboarding.repository.OnboardingRequestRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final OnboardingRequestRepository onboardingRequestRepository;
    private final SupplierRepository supplierRepository;
    private static final List<OnboardingRequestStatus> READABLE_REQUEST_STATUSES =
            List.of(OnboardingRequestStatus.REQUESTED, OnboardingRequestStatus.APPROVED, OnboardingRequestStatus.REJECTED);


    public OnboardingResponse createSupplier(String organizationPublicId, String requestedByUserPublicId, CreateOnboardingRequest request) {
        if (supplierRepository.existsBySupplierCodeAndSupplierStatusNot(request.getSupplierCode(), SupplierStatus.TERMINATED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 협력사 코드입니다.");
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

        OnboardingRequest onboardingRequest = OnboardingRequest.create(savedSupplier, requestedByUserPublicId);

        return OnboardingResponse.fromEntity(onboardingRequestRepository.save(onboardingRequest));
    }

    @Transactional(readOnly = true)
    public OnboardingResponse getRequest(Long requestId) {
        OnboardingRequest request = onboardingRequestRepository.findByIdAndRequestStatusInAndSupplier_SupplierStatusNot(requestId, READABLE_REQUEST_STATUSES,
                        SupplierStatus.TERMINATED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 요청이 존재하지 않습니다."));
        return OnboardingResponse.fromEntity(request);
    }

    @Transactional(readOnly = true)
    public Page<OnboardingResponse> getRequestList(Pageable pageable) {
        return onboardingRequestRepository.findAllByRequestStatusInAndSupplier_SupplierStatusNot( READABLE_REQUEST_STATUSES, SupplierStatus.TERMINATED, pageable)
                .map(OnboardingResponse::fromEntity);
    }

    public OnboardingResponse approveRequest(Long requestId, String reviewedByUserPublicId) {
        OnboardingRequest request = onboardingRequestRepository.findByIdAndRequestStatusAndSupplier_SupplierStatusNot(requestId,
                        OnboardingRequestStatus.REQUESTED,
                        SupplierStatus.TERMINATED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 온보딩 요청이 존재하지 않습니다."));

        if (request.getRequestStatus() != OnboardingRequestStatus.REQUESTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "승인 대기 상태의 요청만 승인할 수 있습니다.");
        }
        request.approve(reviewedByUserPublicId);
        request.getSupplier().approve();

        return OnboardingResponse.fromEntity(request);
    }

    public OnboardingResponse rejectRequest(Long requestId, String reviewedByUserPublicId, RejectOnboardingRequest rejectRequest) {
        OnboardingRequest request = onboardingRequestRepository.findByIdAndRequestStatusAndSupplier_SupplierStatusNot(requestId, OnboardingRequestStatus.REQUESTED, SupplierStatus.TERMINATED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "반려 가능한 온보딩 요청이 존재하지 않습니다."));

        request.reject(reviewedByUserPublicId, rejectRequest.getRejectReason());
        request.getSupplier().reject();

        return OnboardingResponse.fromEntity(request);

    }
}
