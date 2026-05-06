package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.OrganizationType;
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
    // 특정 조직에 속한 사용자들을 한 번에 조회
// 조직 상태 변경 시 소속 사용자 상태를 함께 바꾸는 데 사용
    @EntityGraph(attributePaths = {"organization", "department"})
    List<User> findAllByOrganization_PublicId(String organizationPublicId);

    @EntityGraph(attributePaths = {"organization", "department"})
    List<User> findAllByOrganization_PublicIdAndDepartment_DepartmentCodeAndStatus(
            String organizationPublicId,
            String departmentCode,
            Status status
    );

    @EntityGraph(attributePaths = {"organization", "department"})
    List<User> findAllByOrganization_PublicIdAndUserRoleAndStatus(
            String organizationPublicId,
            UserRole userRole,
            Status status
    );

    @EntityGraph(attributePaths = {"organization", "department"})
    List<User> findAllByOrganization_OrganizationTypeAndUserRoleAndStatus(
            OrganizationType organizationType,
            UserRole userRole,
            Status status
    );

    // 특정 조직에 속한 삭제되지 않은 사용자 수
    long countByOrganization_PublicIdAndStatusNot(
            String organizationPublicId,
            Status status
    );


}
