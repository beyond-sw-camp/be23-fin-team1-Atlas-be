package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;


import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByPublicId(String publicId);
    boolean existsByLoginId(String loginId);
    @EntityGraph(attributePaths = {"organization", "department"})
    Optional<User> findWithOrganizationByUserId(Long userId);
    @EntityGraph(attributePaths = {"organization", "department"})
    List<User> findAllWithOrganizationBy();
    // 해당 조직에 활성 상태 ORG_ADMIN 이 이미 있는지 확인
    boolean existsByOrganization_PublicIdAndUserRoleAndStatus(
            String organizationPublicId,
            UserRole userRole,
            Status status
    );



}
