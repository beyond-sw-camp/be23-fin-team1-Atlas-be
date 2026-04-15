package com.ozz.atlas.supply.batch.repository;

import com.ozz.atlas.supply.batch.domain.ExpiryWarningType;
import com.ozz.atlas.supply.batch.domain.SupplyExpiryWarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SupplyExpiryWarningRepository extends JpaRepository<SupplyExpiryWarning, Long> {

    // 같은 경고 중복 저장 x
    boolean existsByWarningTypeAndSourcePublicIdAndWarningDate(
            ExpiryWarningType warningType,
            String sourcePublicId,
            LocalDate warningDate
    );
}
