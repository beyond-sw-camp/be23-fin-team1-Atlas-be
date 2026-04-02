package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplyItemCategoryRepository extends JpaRepository<SupplyItemCategory, Long> {

    Page<SupplyItemCategory> findAllByStatus(Status status, Pageable pageable);

    Optional<SupplyItemCategory> findByIdAndStatus(Long id, Status status);

    boolean existsByParentCategory_IdAndStatus(Long parentCategoryId, Status status);
}
