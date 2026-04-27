package com.ozz.atlas.supply.supplier.esg.repository;

import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SupplyEsgAssessmentRepository extends JpaRepository<SupplyEsgAssessment, Long> {

    Optional<SupplyEsgAssessment> findTopBySupplier_IdOrderByEvaluatedAtDesc(Long supplierId);

    Page<SupplyEsgAssessment> findAllBySupplier_IdOrderByEvaluatedAtDesc(Long supplierId, Pageable pageable);

    @Query("""
            select assessment
            from SupplyEsgAssessment assessment
            where assessment.evaluatedAt = (
                select max(latest.evaluatedAt)
                from SupplyEsgAssessment latest
                where latest.supplier = assessment.supplier
            )
            order by assessment.totalScore desc, assessment.evaluatedAt desc
            """)
    Page<SupplyEsgAssessment> findLatestAssessmentsBySupplier(Pageable pageable);

}
