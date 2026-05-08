package com.ozz.atlas.supply.supplier.certificate.repository;

import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SupplierCertificateRepository extends JpaRepository<SupplierCertificate, Long> {
    Optional<SupplierCertificate> findByPublicId(String publicId);
    List<SupplierCertificate> findBySupplierPublicId(String supplierPublicId);
    List<SupplierCertificate> findByExpiredAtBetween(LocalDate startDate, LocalDate endDate);
    List<SupplierCertificate> findByExpiredAtAndCertificateStatus(LocalDate expiredAt, CertificateStatus certificateStatus);
    List<SupplierCertificate> findByExpiredAtLessThanEqualAndCertificateStatus(LocalDate expiredAt, CertificateStatus certificateStatus);
    long countBySupplierPublicId(String supplierPublicId);

    @Query("""
        select cert
        from SupplierCertificate cert
        left join SupplySupplier supplier on supplier.publicId = cert.supplierPublicId
        where (:status is null or cert.certificateStatus = :status)
          and (
            :completedOnly = false
            or cert.certificateStatus <> com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus.REVIEW_REQUESTED
          )
          and (
            :keyword is null
            or lower(cert.certificateNo) like lower(concat('%', :keyword, '%'))
            or lower(cert.issuerName) like lower(concat('%', :keyword, '%'))
            or lower(cert.certificateType.certificateName) like lower(concat('%', :keyword, '%'))
            or lower(cert.certificateType.certificateCode) like lower(concat('%', :keyword, '%'))
            or lower(supplier.supplierName) like lower(concat('%', :keyword, '%'))
          )
    """)
    Page<SupplierCertificate> searchForReview(
            @Param("status") CertificateStatus status,
            @Param("completedOnly") boolean completedOnly,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        select count(cert)
        from SupplierCertificate cert
        join SupplySupplier supplier on supplier.publicId = cert.supplierPublicId
        where cert.certificateStatus in :statuses
          and supplier.organizationPublicId = :organizationPublicId
    """)
    long countReadableByOrganizationPublicIdAndStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") List<CertificateStatus> statuses
    );

    @Query("""
        select cert.publicId
        from SupplierCertificate cert
        join SupplySupplier supplier on supplier.publicId = cert.supplierPublicId
        where cert.certificateStatus in :statuses
          and supplier.organizationPublicId = :organizationPublicId
    """)
    List<String> findReadablePublicIdsByOrganizationPublicIdAndStatuses(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("statuses") List<CertificateStatus> statuses
    );
}
