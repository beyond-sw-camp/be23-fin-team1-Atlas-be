package com.ozz.atlas.supply.supplier.relation.repository;

import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupplierRelationRepository extends JpaRepository<SupplySupplierRelation, Long> {

    Optional<SupplySupplierRelation> findByParentSupplier_IdAndChildSupplier_Id(
            Long parentSupplierId,
            Long childSupplierId
    );

    boolean existsByParentSupplier_IdAndChildSupplier_IdAndRelationStatusIn(
            Long parentSupplierId,
            Long childSupplierId,
            Collection<SupplierRelationStatus> relationStatuses
    );

    boolean existsByChildSupplier_IdAndParentSupplier_IdAndRelationStatusIn(
            Long childSupplierId,
            Long parentSupplierId,
            Collection<SupplierRelationStatus> relationStatuses
    );

    @EntityGraph(attributePaths = {"parentSupplier", "childSupplier"})
    List<SupplySupplierRelation> findAllByParentSupplier_IdAndRelationStatusInOrderByPriorityRankAsc(
            Long parentSupplierId,
            Collection<SupplierRelationStatus> relationStatuses
    );

    @EntityGraph(attributePaths = {"parentSupplier", "childSupplier"})
    List<SupplySupplierRelation> findAllByChildSupplier_IdAndRelationStatusInOrderByPriorityRankAsc(
            Long childSupplierId,
            Collection<SupplierRelationStatus> relationStatuses
    );

    @EntityGraph(attributePaths = {"parentSupplier", "childSupplier"})
    Optional<SupplySupplierRelation> findByPublicIdAndParentSupplier_IdAndRelationStatusIn(
            String publicId,
            Long parentSupplierId,
            Collection<SupplierRelationStatus> relationStatuses
    );

    @Query("""
        select count(rel)
        from SupplySupplierRelation rel
        where rel.relationStatus in :relationStatuses
          and (
              rel.parentSupplier.organizationPublicId = :organizationPublicId
              or rel.childSupplier.organizationPublicId = :organizationPublicId
          )
    """)
    long countAttentionRelationsByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("relationStatuses") Collection<SupplierRelationStatus> relationStatuses
    );

    @Query("""
        select rel.publicId
        from SupplySupplierRelation rel
        where rel.relationStatus in :relationStatuses
          and (
              rel.parentSupplier.organizationPublicId = :organizationPublicId
              or rel.childSupplier.organizationPublicId = :organizationPublicId
          )
    """)
    List<String> findAttentionRelationPublicIdsByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("relationStatuses") Collection<SupplierRelationStatus> relationStatuses
    );
}
