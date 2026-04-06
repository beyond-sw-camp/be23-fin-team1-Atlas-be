package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByPublicId(String publicId);
    boolean existsByLoginId(String loginId);
    @EntityGraph(attributePaths = "organization")
    Optional<User> findWithOrganizationByUserId(Long userId);
    @EntityGraph(attributePaths = "organization")
    List<User> findAllWithOrganizationBy();


}
