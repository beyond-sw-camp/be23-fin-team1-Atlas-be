package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findAllByOrderByIdDesc();

//    정산 중복 체크
    boolean existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
            SettlementTargetType targetType,
            String targetPublicId,
            SettlementStatus settlementStatus
    );

}
