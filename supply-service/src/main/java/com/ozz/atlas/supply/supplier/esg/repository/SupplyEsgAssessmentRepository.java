package com.ozz.atlas.supply.supplier.esg.repository;

import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplyEsgAssessmentRepository extends JpaRepository<SupplyEsgAssessment, Long> {

    Optional<SupplyEsgAssessment> findTopBySupplier_IdOrderByEvaluatedAtDesc(Long supplierId);

}

