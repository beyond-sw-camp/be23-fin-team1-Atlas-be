package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

//    정산 중복 체크
    boolean existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
            SettlementTargetType targetType,
            String targetPublicId,
            SettlementStatus settlementStatus
    );
    Optional<Settlement> findByPublicId(String publicId);

    @Query("""
        select s
        from Settlement s
        where s.buyerOrganizationPublicId = :organizationPublicId
           or s.supplierOrganizationPublicId = :organizationPublicId
        """)
    Page<Settlement> findReadableByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            Pageable pageable
    );

    @Query("""
        select s
        from Settlement s
        where s.publicId = :publicId
          and (
              s.buyerOrganizationPublicId = :organizationPublicId
              or s.supplierOrganizationPublicId = :organizationPublicId
          )
        """)
    Optional<Settlement> findReadableByPublicId(
            @Param("publicId") String publicId,
            @Param("organizationPublicId") String organizationPublicId
    );

}
