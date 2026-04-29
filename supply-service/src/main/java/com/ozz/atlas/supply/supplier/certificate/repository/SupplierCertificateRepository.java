package com.ozz.atlas.supply.supplier.certificate.repository;

import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;

@Repository
public interface SupplierCertificateRepository extends JpaRepository<SupplierCertificate, Long> {
    Optional<SupplierCertificate> findByPublicId(String publicId);
    List<SupplierCertificate> findBySupplierPublicId(String supplierPublicId);
    List<SupplierCertificate> findByExpiredAtBetween(LocalDate startDate, LocalDate endDate);
    List<SupplierCertificate> findByExpiredAtAndCertificateStatus(LocalDate expiredAt, CertificateStatus certificateStatus);
    List<SupplierCertificate> findByExpiredAtLessThanEqualAndCertificateStatus(LocalDate expiredAt, CertificateStatus certificateStatus);
    long countBySupplierPublicId(String supplierPublicId);
}
