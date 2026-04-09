package com.ozz.atlas.supply.supplier.relation.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import com.ozz.atlas.supply.supplier.relation.dtos.CreateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.dtos.SupplierRelationResponse;
import com.ozz.atlas.supply.supplier.relation.dtos.UpdateSupplierRelationRequest;
import com.ozz.atlas.supply.supplier.relation.exception.SupplierRelationErrorCode;
import com.ozz.atlas.supply.supplier.relation.exception.SupplierRelationException;
import com.ozz.atlas.supply.supplier.relation.repository.SupplierRelationRepository;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierRelationService {

    private final SupplierRelationRepository supplierRelationRepository;
    private final SupplierRepository supplierRepository;

    public SupplierRelationResponse createRelation(
            String organizationPublicId,
            String parentSupplierPublicId,
            CreateSupplierRelationRequest request
    ) {
        validateDateRange(request.getEffectiveFrom(), request.getEffectiveTo());

        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);
        SupplySupplier childSupplier = getApprovedChildSupplier(request.getChildSupplierPublicId());

        if (parentSupplier.getId().equals(childSupplier.getId())) {
            throw new SupplierRelationException(SupplierRelationErrorCode.SELF_RELATION_NOT_ALLOWED);
        }

        if (supplierRelationRepository.existsByParentSupplier_IdAndChildSupplier_IdAndStatus(
                parentSupplier.getId(),
                childSupplier.getId(),
                Status.ACTIVE
        )) {
            throw new SupplierRelationException(SupplierRelationErrorCode.RELATION_ALREADY_EXISTS);
        }

        SupplySupplierRelation relation = SupplySupplierRelation.create(
                parentSupplier,
                childSupplier,
                request.getRelationType(),
                request.getPriorityRank(),
                request.getEffectiveFrom(),
                request.getEffectiveTo()
        );

        return SupplierRelationResponse.fromEntity(supplierRelationRepository.save(relation));
    }

    @Transactional(readOnly = true)
    public List<SupplierRelationResponse> getRelations(
            String organizationPublicId,
            String parentSupplierPublicId
    ) {
        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);

        return supplierRelationRepository
                .findAllByParentSupplier_IdAndStatusOrderByPriorityRankAsc(
                        parentSupplier.getId(),
                        Status.ACTIVE
                )
                .stream()
                .map(SupplierRelationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierRelationResponse getRelation(
            String organizationPublicId,
            String parentSupplierPublicId,
            String relationPublicId
    ) {
        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);
        SupplySupplierRelation relation = getActiveRelation(parentSupplier.getId(), relationPublicId);

        return SupplierRelationResponse.fromEntity(relation);
    }

    public SupplierRelationResponse updateRelation(
            String organizationPublicId,
            String parentSupplierPublicId,
            String relationPublicId,
            UpdateSupplierRelationRequest request
    ) {
        if (isEmptyPatch(request)) {
            throw new SupplierRelationException(SupplierRelationErrorCode.INVALID_INPUT_VALUE);
        }

        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);
        SupplySupplierRelation relation = getActiveRelation(parentSupplier.getId(), relationPublicId);

        LocalDate effectiveFrom = request.getEffectiveFrom() != null
                ? request.getEffectiveFrom()
                : relation.getEffectiveFrom();

        LocalDate effectiveTo = request.getEffectiveTo() != null
                ? request.getEffectiveTo()
                : relation.getEffectiveTo();

        validateDateRange(effectiveFrom, effectiveTo);

        relation.update(
                request.getRelationType(),
                request.getPriorityRank(),
                request.getEffectiveFrom(),
                request.getEffectiveTo()
        );

        return SupplierRelationResponse.fromEntity(relation);
    }

    public void deleteRelation(
            String organizationPublicId,
            String parentSupplierPublicId,
            String relationPublicId
    ) {
        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);
        SupplySupplierRelation relation = getActiveRelation(parentSupplier.getId(), relationPublicId);

        relation.deactivate(LocalDate.now());
    }

    private SupplySupplier getOwnedParentSupplier(
            String organizationPublicId,
            String parentSupplierPublicId
    ) {
        SupplySupplier parentSupplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        parentSupplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.PARENT_SUPPLIER_NOT_FOUND));

        if (!parentSupplier.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new SupplierRelationException(SupplierRelationErrorCode.ACCESS_DENIED);
        }

        return parentSupplier;
    }

    private SupplySupplier getApprovedChildSupplier(String childSupplierPublicId) {
        return supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        childSupplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.CHILD_SUPPLIER_NOT_FOUND));
    }

    private SupplySupplierRelation getActiveRelation(Long parentSupplierId, String relationPublicId) {
        return supplierRelationRepository.findByPublicIdAndParentSupplier_IdAndStatus(
                        relationPublicId,
                        parentSupplierId,
                        Status.ACTIVE
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.RELATION_NOT_FOUND));
    }

    private void validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new SupplierRelationException(SupplierRelationErrorCode.INVALID_DATE_RANGE);
        }
    }

    private boolean isEmptyPatch(UpdateSupplierRelationRequest request) {
        return request.getRelationType() == null
                && request.getPriorityRank() == null
                && request.getEffectiveFrom() == null
                && request.getEffectiveTo() == null;
    }
}
