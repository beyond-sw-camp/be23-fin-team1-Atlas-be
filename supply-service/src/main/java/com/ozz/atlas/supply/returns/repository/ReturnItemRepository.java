package com.ozz.atlas.supply.returns.repository;

import com.ozz.atlas.supply.returns.domain.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
}