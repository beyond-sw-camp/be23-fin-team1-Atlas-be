package com.ozz.atlas.supply.productionline.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
    boolean existsByLogisticsNodePublicIdAndLineCodeAndStatusIn(
            String logisticsNodePublicId,
            String lineCode,
            List<Status> statuses
    );
    Page<ProductionLine>findAllByStatus(Status status, Pageable pageable);
    Optional<ProductionLine>findByProductionLineIdAndStatus(Long productionLineId, Status status);
    boolean existsByLogisticsNodePublicIdAndLineCodeAndProductionLineIdNotAndStatusIn(
            String logisticsNodePublicId,
            String lineCode,
            Long productionLineId,
            List<Status> statuses
    );
}
