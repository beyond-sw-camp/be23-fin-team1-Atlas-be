package com.ozz.atlas.supply.logistics.repository;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogisticsNodeRepository extends JpaRepository<LogisticsNode, Long> {

    Optional<LogisticsNode> findByPublicId(String publicId);

//    목록 조회 시 필요한 node id를 한번에 모아서 조회
    List<LogisticsNode> findByIdIn(Collection<Long> ids);

//    창고 생성
    boolean existsByNodeCode(String nodeCode);

//    창고 수정시 중복검사
    boolean existsByNodeCodeAndPublicIdNot(String nodeCode, String publicId);

    long countByOrganizationPublicId(String organizationPublicId);

    Page<LogisticsNode> findByOrganizationPublicId(String organizationPublicId, Pageable pageable);

    Optional<LogisticsNode> findByPublicIdAndOrganizationPublicId(String publicId, String organizationPublicId);

}
