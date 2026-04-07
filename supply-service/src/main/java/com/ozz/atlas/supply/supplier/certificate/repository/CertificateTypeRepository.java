package com.ozz.atlas.supply.supplier.certificate.repository;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateTypeRepository extends JpaRepository<CertificateType, Long> {
    Optional<CertificateType> findByCertificateCode(String certificateCode);
}