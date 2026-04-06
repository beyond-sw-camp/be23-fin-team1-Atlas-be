package com.ozz.atlas.supply.logistics.repository;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogisticsNodeRepository extends JpaRepository<LogisticsNode, Long> {

    Optional<LogisticsNode> findByPublicId(String publicId);

//    창고 생성
    boolean existsByNodeCode(String nodeCode);

//    창고 수정시 중복검사
    boolean existsByNodeCodeAndPublicIdNot(String nodeCode, String publicId);
}
