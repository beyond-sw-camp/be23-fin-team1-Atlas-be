package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    List<SettlementDetail> findAllBySettlement_IdOrderByIdAsc(Long settlementId);

    Optional<SettlementDetail> findByPublicId(String publicId);
}
