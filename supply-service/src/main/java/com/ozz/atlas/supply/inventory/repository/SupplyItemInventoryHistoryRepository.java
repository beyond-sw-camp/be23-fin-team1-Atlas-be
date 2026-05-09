package com.ozz.atlas.supply.inventory.repository;

import com.ozz.atlas.supply.inventory.domain.SupplyItemInventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyItemInventoryHistoryRepository extends JpaRepository<SupplyItemInventoryHistory, Long> {

    List<SupplyItemInventoryHistory> findByInventoryIdOrderByRecordedAtDesc(Long inventoryId);
}
