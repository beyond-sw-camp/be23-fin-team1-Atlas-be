package com.ozz.atlas.supply.returns.repository;

import com.ozz.atlas.supply.returns.domain.ReturnStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnStatusHistoryRepository extends JpaRepository<ReturnStatusHistory, Long> {
    List<ReturnStatusHistory> findByReturnRequestIdOrderByRecordedAtDesc(Long returnRequestId);
}