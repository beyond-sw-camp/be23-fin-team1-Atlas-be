package com.ozz.atlas.supply.inventory.repository;

import com.ozz.atlas.supply.inventory.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByInventoryIdOrderByTransactionAtDesc(Long inventoryId);
    List<InventoryTransaction> findByItemPublicIdOrderByTransactionAtDesc(String itemPublicId);
}
