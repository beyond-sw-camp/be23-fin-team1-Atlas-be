package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {

    Page<SupplyItem> findAllByStatus(Status status, Pageable pageable);

    Optional<SupplyItem> findByIdAndStatusIn(Long itemId, Collection<Status> status);

    boolean existsByItemCode(String itemCode);

    boolean existsByItemCodeAndIdNot(String itemCode, Long id);

    boolean existsByItemCategoryAndStatus(SupplyItemCategory category, Status status);

}
