package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {

    Page<SupplyItem> findAllByActiveYn(Integer activeYn, Pageable pageable);

    Optional<SupplyItem> findByPublicIdAndActiveYn(String publicId, Integer activeYn);

    boolean existsByItemCode(String itemCode);

    boolean existsByItemCodeAndIdNot(String itemCode, Long id);

    boolean existsByItemCategoryAndActiveYn(SupplyItemCategory category, Integer activeYn);

}
