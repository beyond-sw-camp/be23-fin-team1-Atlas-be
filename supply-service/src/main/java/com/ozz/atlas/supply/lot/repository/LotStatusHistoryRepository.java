package com.ozz.atlas.supply.lot.repository;

import com.ozz.atlas.supply.lot.domain.LotStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LotStatusHistoryRepository extends JpaRepository<LotStatusHistory, Long> {
    List<LotStatusHistory> findByLot_PublicIdOrderByCreatedAtDesc(String lotPublicId);
}
