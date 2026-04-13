package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findAllByOrderByIdDesc();

}
