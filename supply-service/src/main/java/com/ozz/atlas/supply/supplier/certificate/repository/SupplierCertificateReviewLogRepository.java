package com.ozz.atlas.supply.supplier.certificate.repository;

import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateReviewLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierCertificateReviewLogRepository extends JpaRepository<SupplierCertificateReviewLog, Long> {

    List<SupplierCertificateReviewLog> findBySupplierCertificateIdOrderByReviewedAtDesc(Long supplierCertificateId);
}
