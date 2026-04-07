package com.ozz.atlas.supply.supplier.certificate.repository;

import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierCertificateHistoryRepository extends JpaRepository<SupplierCertificateHistory, Long> {
    List<SupplierCertificateHistory> findBySupplierCertificateIdOrderByRecordedAtDesc(Long supplierCertificateId);
}