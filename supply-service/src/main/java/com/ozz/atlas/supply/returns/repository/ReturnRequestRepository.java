package com.ozz.atlas.supply.returns.repository;

import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import java.util.Collection;


import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByPublicId(String publicId);

    Optional<ReturnRequest> findByPublicIdAndRequestOrganizationPublicIdOrPublicIdAndTargetOrganizationPublicId(
            String publicId,
            String requestOrganizationPublicId,
            String samePublicId,
            String targetOrganizationPublicId
    );

    org.springframework.data.domain.Page<ReturnRequest> findByRequestOrganizationPublicIdOrTargetOrganizationPublicId(
            String requestOrganizationPublicId,
            String targetOrganizationPublicId,
            org.springframework.data.domain.Pageable pageable
    );
    boolean existsBySourceShipmentPublicId(String sourceShipmentPublicId);

    Optional<ReturnRequest> findByReturnShipmentPublicId(String returnShipmentPublicId);

    long countByReturnStatusInAndRequestOrganizationPublicIdOrReturnStatusInAndTargetOrganizationPublicId(
            Collection<ReturnStatus> requestStatuses,
            String requestOrganizationPublicId,
            Collection<ReturnStatus> targetStatuses,
            String targetOrganizationPublicId
    );


}