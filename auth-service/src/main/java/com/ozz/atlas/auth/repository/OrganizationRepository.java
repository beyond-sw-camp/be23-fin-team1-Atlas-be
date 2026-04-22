package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    Optional<Organization> findByPublicId(String publicId);

//    organizationAlias는 조직 코드로 사용되므로 등록/수정 시 중복 여부를 별도로 확인한다.
    boolean existsByOrganizationAlias(String organizationAlias);

    boolean existsByOrganizationAliasAndOrganizationIdNot(String organizationAlias, Long organizationId);

}
