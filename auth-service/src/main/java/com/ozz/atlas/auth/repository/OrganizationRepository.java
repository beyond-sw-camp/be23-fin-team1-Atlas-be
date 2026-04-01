package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByPublicId(String publicId);
}
