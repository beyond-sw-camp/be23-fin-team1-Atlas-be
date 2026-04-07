package com.ozz.atlas.supply.supplier.repository;

import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplySupplier, Long> {

    Page<SupplySupplier> findAllBySupplierStatusNot(
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Page<SupplySupplier> findAllByApprovalStatusAndSupplierStatusNot(
            ApprovalStatus approvalStatus,
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Page<SupplySupplier> findAllByTierLevelAndApprovalStatusAndSupplierStatusNot(
            Integer tierLevel,
            ApprovalStatus approvalStatus,
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Optional<SupplySupplier> findByPublicIdAndSupplierStatusNot(
            String publicId,
            SupplierStatus supplierStatus
    );

    Optional<SupplySupplier> findByPublicIdAndApprovalStatusAndSupplierStatusNot(
            String publicId,
            ApprovalStatus approvalStatus,
            SupplierStatus supplierStatus
    );

    boolean existsBySupplierCodeAndSupplierStatusNot(
            String supplierCode,
            SupplierStatus supplierStatus
    );

    boolean existsBySupplierCodeAndPublicIdNotAndSupplierStatusNot(
            String supplierCode,
            String publicId,
            SupplierStatus supplierStatus
    );
}
