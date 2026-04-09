package com.ozz.atlas.supply.supplier.relation.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRelationRepository extends JpaRepository<SupplySupplierRelation, Long> {

    boolean existsByParentSupplier_IdAndChildSupplier_IdAndStatus(
            Long parentSupplierId,
            Long childSupplierId,
            Status status
    );

    @EntityGraph(attributePaths = {"parentSupplier", "childSupplier"})
    List<SupplySupplierRelation> findAllByParentSupplier_IdAndStatusOrderByPriorityRankAsc(
            Long parentSupplierId,
            Status status
    );

    @EntityGraph(attributePaths = {"parentSupplier", "childSupplier"})
    Optional<SupplySupplierRelation> findByPublicIdAndParentSupplier_IdAndStatus(
            String publicId,
            Long parentSupplierId,
            Status status
    );
}
