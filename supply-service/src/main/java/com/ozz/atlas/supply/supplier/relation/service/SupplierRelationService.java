package com.ozz.atlas.supply.supplier.relation.service;

import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.relation.domain.SupplierRelationStatus;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierRelationService {

    private static final List<SupplierRelationStatus> VISIBLE_RELATION_STATUSES = List.of(
            SupplierRelationStatus.REQUESTED,
            SupplierRelationStatus.ACTIVE,
            SupplierRelationStatus.PAUSED
    );

    private final SupplierRelationRepository supplierRelationRepository;
    private final SupplierRepository supplierRepository;

    public SupplierRelationResponse createRelation(
            String organizationPublicId,
            String parentSupplierPublicId,
            CreateSupplierRelationRequest request
    ) {
        validateDateRange(request.getEffectiveFrom(), request.getEffectiveTo());

        SupplySupplier parentSupplier = getOwnedParentSupplier(organizationPublicId, parentSupplierPublicId);
        SupplySupplier childSupplier = getChildSupplierOrThrow(request.getChildSupplierPublicId());

        validateNotSelf(parentSupplier, childSupplier);

        SupplySupplierRelation existing = supplierRelationRepository
                .findByParentSupplier_IdAndChildSupplier_Id(parentSupplier.getId(), childSupplier.getId())
                .orElse(null);

        if (existing != null) {
            if (existing.getRelationStatus() != SupplierRelationStatus.ENDED) {
                throw new SupplierRelationException(SupplierRelationErrorCode.RELATION_ALREADY_EXISTS);
            }

            existing.update(
                    request.getPriorityRank(),
                    request.getEffectiveFrom(),
                    request.getEffectiveTo()
            );
            existing.markRequested();
            return SupplierRelationResponse.fromEntity(existing);
        }

        SupplySupplierRelation relation = SupplySupplierRelation.create(
                parentSupplier,
                childSupplier,
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
                .findAllByParentSupplier_IdAndRelationStatusInOrderByPriorityRankAsc(
                        parentSupplier.getId(),
                        VISIBLE_RELATION_STATUSES
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
        SupplySupplierRelation relation = getVisibleRelation(parentSupplier.getId(), relationPublicId);

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
        SupplySupplierRelation relation = getVisibleRelation(parentSupplier.getId(), relationPublicId);

        LocalDate effectiveFrom = request.getEffectiveFrom() != null
                ? request.getEffectiveFrom()
                : relation.getEffectiveFrom();

        LocalDate effectiveTo = request.getEffectiveTo() != null
                ? request.getEffectiveTo()
                : relation.getEffectiveTo();

        validateDateRange(effectiveFrom, effectiveTo);

        relation.update(
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
        SupplySupplierRelation relation = getVisibleRelation(parentSupplier.getId(), relationPublicId);

        relation.markEnded(LocalDate.now());
    }

    public void syncRelationStatus(
            SupplySupplier parentSupplier,
            SupplySupplier childSupplier,
            SupplierRelationStatus relationStatus
    ) {
        SupplySupplierRelation relation = supplierRelationRepository
                .findByParentSupplier_IdAndChildSupplier_Id(parentSupplier.getId(), childSupplier.getId())
                .orElseGet(() -> supplierRelationRepository.save(
                        SupplySupplierRelation.create(
                                parentSupplier,
                                childSupplier,
                                1,
                                LocalDate.now(),
                                null
                        )
                ));

        switch (relationStatus) {
            case REQUESTED -> relation.markRequested();
            case ACTIVE -> relation.markActive();
            case PAUSED -> relation.markPaused();
            case ENDED -> relation.markEnded(LocalDate.now());
        }
    }

    @Transactional(readOnly = true)
    public boolean hasVisibleRelation(Long supplierId, Long relatedSupplierId) {
        return supplierRelationRepository.existsByParentSupplier_IdAndChildSupplier_IdAndRelationStatusIn(
                supplierId,
                relatedSupplierId,
                VISIBLE_RELATION_STATUSES
        ) || supplierRelationRepository.existsByChildSupplier_IdAndParentSupplier_IdAndRelationStatusIn(
                supplierId,
                relatedSupplierId,
                VISIBLE_RELATION_STATUSES
        );
    }

    @Transactional(readOnly = true)
    public List<SupplySupplierRelation> getVisibleRelations(Long supplierId) {
        List<SupplySupplierRelation> parentRelations =
                supplierRelationRepository.findAllByParentSupplier_IdAndRelationStatusInOrderByPriorityRankAsc(
                        supplierId,
                        VISIBLE_RELATION_STATUSES
                );

        List<SupplySupplierRelation> childRelations =
                supplierRelationRepository.findAllByChildSupplier_IdAndRelationStatusInOrderByPriorityRankAsc(
                        supplierId,
                        VISIBLE_RELATION_STATUSES
                );

        return Stream.concat(parentRelations.stream(), childRelations.stream())
                .sorted(Comparator.comparing(SupplySupplierRelation::getPriorityRank)
                        .thenComparing(SupplySupplierRelation::getCreatedAt))
                .toList();
    }

    private SupplySupplier getOwnedParentSupplier(
            String organizationPublicId,
            String parentSupplierPublicId
    ) {
        SupplySupplier parentSupplier = supplierRepository.findByPublicIdAndSupplierStatusNot(
                        parentSupplierPublicId,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.PARENT_SUPPLIER_NOT_FOUND));

        if (!parentSupplier.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new SupplierRelationException(SupplierRelationErrorCode.ACCESS_DENIED);
        }

        return parentSupplier;
    }

    private SupplySupplier getChildSupplierOrThrow(String childSupplierPublicId) {
        return supplierRepository.findByPublicIdAndSupplierStatusNot(
                        childSupplierPublicId,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.CHILD_SUPPLIER_NOT_FOUND));
    }

    private SupplySupplierRelation getVisibleRelation(Long parentSupplierId, String relationPublicId) {
        return supplierRelationRepository.findByPublicIdAndParentSupplier_IdAndRelationStatusIn(
                        relationPublicId,
                        parentSupplierId,
                        VISIBLE_RELATION_STATUSES
                )
                .orElseThrow(() -> new SupplierRelationException(SupplierRelationErrorCode.RELATION_NOT_FOUND));
    }

    private void validateNotSelf(SupplySupplier parentSupplier, SupplySupplier childSupplier) {
        if (parentSupplier.getId().equals(childSupplier.getId())) {
            throw new SupplierRelationException(SupplierRelationErrorCode.SELF_RELATION_NOT_ALLOWED);
        }
    }

    private void validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new SupplierRelationException(SupplierRelationErrorCode.INVALID_DATE_RANGE);
        }
    }

    private boolean isEmptyPatch(UpdateSupplierRelationRequest request) {
        return request.getPriorityRank() == null
                && request.getEffectiveFrom() == null
                && request.getEffectiveTo() == null;
    }
}
