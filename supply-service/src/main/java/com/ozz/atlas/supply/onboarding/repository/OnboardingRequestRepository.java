package com.ozz.atlas.supply.onboarding.repository;

import com.ozz.atlas.supply.onboarding.domain.OnboardingRequest;
import com.ozz.atlas.supply.onboarding.domain.OnboardingRequestStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface OnboardingRequestRepository extends JpaRepository<OnboardingRequest, Long> {

    Page<OnboardingRequest> findAllByRequestStatusInAndSupplier_SupplierStatusNot(
            Collection<OnboardingRequestStatus> requestStatuses,
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Page<OnboardingRequest> findAllByRequestStatusInAndSupplier_OrganizationPublicIdAndSupplier_SupplierStatusNot(
            Collection<OnboardingRequestStatus> requestStatuses,
            String organizationPublicId,
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Optional<OnboardingRequest> findByPublicIdAndRequestStatusInAndSupplier_SupplierStatusNot(
            String publicId,
            Collection<OnboardingRequestStatus> requestStatuses,
            SupplierStatus supplierStatus
    );

    Optional<OnboardingRequest> findByPublicIdAndRequestStatusAndSupplier_SupplierStatusNot(
            String publicId,
            OnboardingRequestStatus requestStatus,
            SupplierStatus supplierStatus
    );
}
