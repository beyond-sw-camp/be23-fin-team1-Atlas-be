package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.SettlementBudget;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementBudgetRepository extends JpaRepository<SettlementBudget, Long> {

    // 특정 조직의 특정 연도 예산 목록을 조회
    List<SettlementBudget> findAllByOrganizationPublicIdAndYear(
            String organizationPublicId,
            Integer year
    );

    // 특정 조직의 특정 월 예산을 조회
    Optional<SettlementBudget> findByOrganizationPublicIdAndYearAndMonthAndCurrencyCode(
            String organizationPublicId,
            Integer year,
            Integer month,
            SettlementCurrency currencyCode
    );
}
