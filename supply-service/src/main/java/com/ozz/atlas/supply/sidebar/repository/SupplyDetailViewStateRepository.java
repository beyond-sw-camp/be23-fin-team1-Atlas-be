package com.ozz.atlas.supply.sidebar.repository;

import com.ozz.atlas.supply.sidebar.domain.SupplyDetailViewState;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyDetailViewStateRepository extends JpaRepository<SupplyDetailViewState, Long> {

    Optional<SupplyDetailViewState> findByUserPublicIdAndOrganizationPublicIdAndMenuKeyAndDetailPublicId(
            String userPublicId,
            String organizationPublicId,
            String menuKey,
            String detailPublicId
    );

    long countByUserPublicIdAndOrganizationPublicIdAndMenuKeyAndDetailPublicIdIn(
            String userPublicId,
            String organizationPublicId,
            String menuKey,
            Collection<String> detailPublicIds
    );
}
