package com.ozz.atlas.supply.logistics.repository;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
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

    Page<LogisticsNode> findByOrganizationPublicId(String organizationPublicId, Pageable pageable);

    List<LogisticsNode> findByOrganizationPublicId(String organizationPublicId);

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicId(String publicId, String organizationPublicId);

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicIdAndActiveTrue(
            String publicId,
            String organizationPublicId
    );
}
