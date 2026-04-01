package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplyItemCategoryRepository extends JpaRepository<SupplyItemCategory, Long> {

    Page<SupplyItemCategory> findAllByActiveYn(Integer activeYn, Pageable pageable);

    Optional<SupplyItemCategory> findByIdAndActiveYn(Long id, Integer activeYn);

    boolean existsByParentCategory_IdAndActiveYn(Long parentCategoryId, Integer activeYn);
}
