package com.ozz.atlas.supply.logistics.repository;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LogisticsNodeRepository extends JpaRepository<LogisticsNode, Long> {

    Optional<LogisticsNode> findByPublicId(String publicId);

    List<LogisticsNode> findByIdIn(Collection<Long> ids);

    boolean existsByNodeCode(String nodeCode);

    boolean existsByNodeCodeAndPublicIdNot(String nodeCode, String publicId);

    long countByOrganizationPublicId(String organizationPublicId);

    long countByOrganizationPublicIdAndActiveFalse(String organizationPublicId);

    long countByOrganizationPublicIdAndCapacityStatus(
            String organizationPublicId,
            LogisticsNodeCapacityStatus capacityStatus
    );

    List<LogisticsNode> findByOrganizationPublicIdAndActiveFalse(String organizationPublicId);

    List<LogisticsNode> findByOrganizationPublicIdAndCapacityStatus(
            String organizationPublicId,
            LogisticsNodeCapacityStatus capacityStatus
    );

    Page<LogisticsNode> findByOrganizationPublicId(String organizationPublicId, Pageable pageable);

    Page<LogisticsNode> findByOrganizationPublicIdAndNodeType(
            String organizationPublicId,
            LogisticsNodeType nodeType,
            Pageable pageable
    );

    List<LogisticsNode> findByOrganizationPublicId(String organizationPublicId);

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicId(String publicId, String organizationPublicId);

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicIdAndNodeType(
            String publicId,
            String organizationPublicId,
            LogisticsNodeType nodeType
    );

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicIdAndActiveTrue(
            String publicId,
            String organizationPublicId
    );
}
