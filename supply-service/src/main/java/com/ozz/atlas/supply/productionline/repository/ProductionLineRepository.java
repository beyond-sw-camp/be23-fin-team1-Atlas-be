package com.ozz.atlas.supply.productionline.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
    boolean existsByLogisticsNodePublicIdAndLineCodeAndStatusIn(
            String logisticsNodePublicId,
            String lineCode,
            List<Status> statuses
    );
}
