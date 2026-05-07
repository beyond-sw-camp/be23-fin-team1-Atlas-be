package com.ozz.atlas.supply.logistics.repository;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticsNodeHistoryRepository extends JpaRepository<LogisticsNodeHistory, Long> {

    List<LogisticsNodeHistory> findByLogisticsNodeIdOrderByRecordedAtDesc(Long logisticsNodeId);
}
