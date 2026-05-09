package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.supply.item.domain.SupplyItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyItemHistoryRepository extends JpaRepository<SupplyItemHistory, Long> {

    List<SupplyItemHistory> findByItemIdOrderByRecordedAtDesc(Long itemId);
}
