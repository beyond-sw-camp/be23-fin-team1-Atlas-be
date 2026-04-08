package com.ozz.atlas.supply.returns.repository;

import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByPublicId(String publicId);
}