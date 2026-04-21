package com.ozz.atlas.control.recommendation.repository;

import com.ozz.atlas.control.recommendation.domain.Recommendation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    Optional<Recommendation> findByPublicId(String publicId);

    Optional<Recommendation> findBySourceEventId(String sourceEventId);

    Optional<Recommendation> findByPublicIdAndOrganizationPublicId(String publicId, String organizationPublicId);

    Page<Recommendation> findByOrganizationPublicIdOrderByCreatedAtDesc(String organizationPublicId, Pageable pageable);
}
