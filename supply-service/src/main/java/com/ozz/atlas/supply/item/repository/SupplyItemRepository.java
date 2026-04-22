package com.ozz.atlas.supply.item.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {

    Page<SupplyItem> findAllByStatus(Status status, Pageable pageable);

    Optional<SupplyItem> findByPublicIdAndStatusIn(String publicId, Collection<Status> status);

    Optional<SupplyItem> findByPublicId(String publicId);

    List<SupplyItem> findAllByPublicIdIn(Collection<String> publicIds);

    List<SupplyItem> findAllByPublicIdInAndStatus(Collection<String> publicIds, Status status);

    boolean existsByItemCode(String itemCode);

    boolean existsByItemCodeAndIdNot(String itemCode, Long id);

    boolean existsByItemCategoryAndStatus(SupplyItemCategory category, Status status);

    boolean existsByItemCategoryAndStatusIn(SupplyItemCategory category, Collection<Status> statuses);

    List<SupplyItem> findAllBySupplier_IdAndStatusIn(Long supplierId, Collection<Status> statuses);

    Optional<SupplyItem> findTopByItemCodeStartingWithOrderByItemCodeDesc(String prefix);


}
