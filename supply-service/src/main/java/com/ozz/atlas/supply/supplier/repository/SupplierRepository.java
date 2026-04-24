package com.ozz.atlas.supply.supplier.repository;

import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplySupplier, Long> {

    Page<SupplySupplier> findAllBySupplierStatusNot(
            SupplierStatus supplierStatus,
            Pageable pageable
    );

    Optional<SupplySupplier> findByPublicId(String publicId);

    Optional<SupplySupplier> findByPublicIdAndSupplierStatusNot(
            String publicId,
            SupplierStatus supplierStatus
    );

    boolean existsBySupplierCodeAndSupplierStatusNot(
            String supplierCode,
            SupplierStatus supplierStatus
    );

    boolean existsBySupplierCodeAndIdNotAndSupplierStatusNot(
            String supplierCode,
            Long id,
            SupplierStatus supplierStatus
    );

    Optional<SupplySupplier> findByOrganizationPublicId(String organizationPublicId);

    boolean existsByOrganizationPublicId(String organizationPublicId);

    Optional<SupplySupplier> findByPublicIdAndOrganizationPublicIdAndSupplierStatusNot(
            String publicId,
            String organizationPublicId,
            SupplierStatus supplierStatus
    );
}
